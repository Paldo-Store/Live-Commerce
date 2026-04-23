package com.live_commerce.payment.application.service;

import java.util.List;
import java.util.UUID;

import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestClientException;

import com.live_commerce.payment.application.dto.request.PaymentApproveRequestDto;
import com.live_commerce.payment.application.dto.request.PaymentReadyRequestDto;
import com.live_commerce.payment.application.dto.response.PaymentApproveResponseDto;
import com.live_commerce.payment.application.dto.response.PaymentGetResponseDto;
import com.live_commerce.payment.application.dto.response.PaymentReadyResponseDto;
import com.live_commerce.payment.application.dto.response.PaymentRefundResponseDto;
import com.live_commerce.payment.application.exception.CustomException;
import com.live_commerce.payment.application.exception.PaymentExceptionCode;
import com.live_commerce.payment.application.port.KakaoPayClient;
import com.live_commerce.payment.infrastructure.redis.PaymentRedisKeys;
import com.live_commerce.payment.domain.event.PaymentCompletedDomainEvent;
import com.live_commerce.payment.domain.event.PaymentFailedDomainEvent;
import com.live_commerce.payment.domain.event.PaymentReadyDomainEvent;
import com.live_commerce.payment.domain.model.Payment;
import com.live_commerce.payment.domain.model.PaymentStatus;
import com.live_commerce.payment.domain.repository.PaymentRepository;
import com.live_commerce.payment.domain.repository.PaymentSearchCondition;
import com.live_commerce.payment.infrastructure.client.OrderClient;
import com.live_commerce.payment.infrastructure.client.dto.KakaoPayApproveDto;
import com.live_commerce.payment.infrastructure.client.dto.KakaoPayReadyDto;
import com.live_commerce.payment.infrastructure.client.dto.PaymentCancelRequest;
import com.live_commerce.payment.infrastructure.lock.DistributedLock;
import com.live_commerce.payment.infrastructure.security.RequestUserDetails;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceV2 {

	private final PaymentRepository paymentRepository;
	private final KakaoPayClient kakaoPayClient;
	private final OrderClient orderClient;
	private final RedissonClient redissonClient;
	private final ApplicationEventPublisher eventPublisher;
	private final TransactionTemplate requiresNewTransactionTemplate;

	@DistributedLock(key = "#dto.orderId")
	@Transactional
	public PaymentReadyResponseDto readyPayment(RequestUserDetails user, PaymentReadyRequestDto dto) {
		paymentRepository.findByOrderId(dto.orderId()).ifPresent(existing -> {
			if (existing.getStatus() != PaymentStatus.FAILED) {
				throw new CustomException(PaymentExceptionCode.DUPLICATE_PAYMENT);
			}
		});

		KakaoPayReadyDto readyDto = kakaoPayClient.requestKakaoPayReady(
			user.getUserId(), dto.orderId(), dto.amount(), dto.itemName()
		);

		Payment payment = dto.toEntity(user.getUserId());
		payment.assignTid(readyDto.tid());
		paymentRepository.save(payment);

		// DB 커밋 완료 후 Redis key 설정 (PaymentDomainEventListener.onPaymentReady)
		eventPublisher.publishEvent(new PaymentReadyDomainEvent(dto.orderId(), payment.getId()));

		return PaymentReadyResponseDto.from(readyDto);
	}

	public PaymentApproveResponseDto approvePayment(PaymentApproveRequestDto requestDto, UUID userId) {
		UUID orderId = UUID.fromString(requestDto.orderId());

		// ── 1. 사전 검증 [no tx] ──────────────────────────────────
		Payment payment = paymentRepository.findByOrderId(orderId)
			.orElseThrow(() -> new CustomException(PaymentExceptionCode.NOT_FOUND));

		if (payment.getStatus() != PaymentStatus.PENDING) {
			throw new CustomException(PaymentExceptionCode.INVALID_STATUS);
		}

		RBucket<String> expireBucket = redissonClient.getBucket(PaymentRedisKeys.EXPIRE_KEY_PREFIX + orderId);
		if (!expireBucket.isExists()) {
			throw new CustomException(PaymentExceptionCode.PAYMENT_EXPIRED);
		}

		// ── 2. 카카오 API 호출 [no tx, 커넥션 미점유] ──────────────
		KakaoPayApproveDto approveDto;
		try {
			approveDto = kakaoPayClient.requestKakaoPayApprove(
				requestDto.tid(), requestDto.pgToken(), requestDto.orderId(), userId.toString()
			);
		} catch (RestClientException e) {
			requiresNewTransactionTemplate.executeWithoutResult(status -> {
				Payment p = paymentRepository.findByOrderId(orderId)
					.orElseThrow(() -> new CustomException(PaymentExceptionCode.NOT_FOUND));
				p.fail();
				eventPublisher.publishEvent(new PaymentFailedDomainEvent(p.getOrderId(), "카카오페이 승인 실패"));
			});
			throw new CustomException(PaymentExceptionCode.PAYMENT_APPROVE_FAIL);
		}

		// ── 3. DB 상태 업데이트 [REQUIRES_NEW tx] ──────────────────────
		try {
			requiresNewTransactionTemplate.executeWithoutResult(status -> {
				Payment p = paymentRepository.findByOrderId(orderId)
					.orElseThrow(() -> new CustomException(PaymentExceptionCode.NOT_FOUND));
				p.complete();
				eventPublisher.publishEvent(new PaymentCompletedDomainEvent(p.getOrderId(), p.getAmount()));
			});
		} catch (RuntimeException e) {
			log.error("[Payment] DB 업데이트 실패 - 카카오 보상 취소 시작: orderId={}", orderId, e);
			try {
				kakaoPayClient.requestKakaoPayCancel(payment.getTid(), payment.getAmount());
			} catch (RestClientException ex) {
				log.error("[Payment] 보상 취소 실패 - 수동 처리 필요: orderId={}", orderId, ex);
			}
			throw new CustomException(PaymentExceptionCode.PAYMENT_APPROVE_FAIL);
		}

		// ── 4. Redis key 삭제 [no tx] ─────────────────────────────
		expireBucket.delete();

		return PaymentApproveResponseDto.from(approveDto);
	}

	@Transactional(readOnly = true)
	public PaymentGetResponseDto getPayment(UUID paymentId, RequestUserDetails userDetails) {
		Payment payment = findPaymentById(paymentId);
		validatePaymentOwnerPermission(payment, userDetails);
		return PaymentGetResponseDto.from(payment);
	}

	@Transactional(readOnly = true)
	public Page<PaymentGetResponseDto> getPayments(
		PaymentSearchCondition condition,
		RequestUserDetails userDetails,
		Pageable pageable
	) {
		int size = pageable.getPageSize();
		if (size != 10 && size != 30 && size != 50) {
			pageable = PageRequest.of(pageable.getPageNumber(), 10, pageable.getSort());
		}

		PaymentSearchCondition finalCondition = hasMasterRole(userDetails) ? condition :
			new PaymentSearchCondition(
				userDetails.getUserId(),
				condition.orderId(),
				condition.status(),
				condition.createdAtFrom(),
				condition.createdAtTo()
			);

		List<Payment> payments = paymentRepository.searchPayment(finalCondition, pageable);
		long totalCount = paymentRepository.countPayment(finalCondition);

		return new PageImpl<>(
			payments.stream().map(PaymentGetResponseDto::from).toList(),
			pageable,
			totalCount
		);
	}

	@Transactional
	public PaymentRefundResponseDto refundPaymentByOrderId(UUID orderId, RequestUserDetails userDetails) {
		Payment payment = findPaymentByOrderId(orderId);
		validatePaymentOwnerPermission(payment, userDetails);

		if (payment.getStatus() != PaymentStatus.COMPLETED) {
			throw new CustomException(PaymentExceptionCode.INVALID_STATUS);
		}

		kakaoPayClient.requestKakaoPayCancel(payment.getTid(), payment.getAmount());
		payment.refund();

		try {
			orderClient.notifyOrderCancel(orderId, new PaymentCancelRequest(false, "결제 취소 처리됨"));
		} catch (Exception e) {
			log.warn("주문 서비스에 결제 취소 알림 실패: {}", e.getMessage());
		}

		return PaymentRefundResponseDto.from(payment);
	}

	@Transactional
	public void cancelPaymentByOrderId(UUID orderId, RequestUserDetails userDetails) {
		Payment payment = findPaymentByOrderId(orderId);
		validatePaymentOwnerPermission(payment, userDetails);

		if (payment.getStatus() != PaymentStatus.PENDING) {
			throw new CustomException(PaymentExceptionCode.INVALID_STATUS);
		}

		try {
			orderClient.notifyOrderCancel(orderId, new PaymentCancelRequest(false, "결제 취소 처리됨"));
		} catch (Exception e) {
			log.warn("주문 서비스에 결제 취소 알림 실패: {}", e.getMessage());
		}

		payment.cancel();
	}

	@Transactional
	public void compensateRefundByOrderId(UUID orderId, String message) {
		Payment payment = paymentRepository.findByOrderId(orderId)
			.orElseThrow(() -> new CustomException(PaymentExceptionCode.NOT_FOUND));

		if (payment.getStatus() != PaymentStatus.COMPLETED) {
			log.info("[Payment] 보상 처리 스킵: orderId={}, status={}", orderId, payment.getStatus());
			return;
		}

		kakaoPayClient.requestKakaoPayCancel(payment.getTid(), payment.getAmount());
		payment.refund();

		log.info("[Payment] 보상 결제 취소 완료: orderId={}, message={}", orderId, message);
	}

	private Payment findPaymentById(UUID paymentId) {
		return paymentRepository.findById(paymentId)
			.orElseThrow(() -> new CustomException(PaymentExceptionCode.NOT_FOUND));
	}

	private Payment findPaymentByOrderId(UUID orderId) {
		return paymentRepository.findByOrderId(orderId)
			.orElseThrow(() -> new CustomException(PaymentExceptionCode.NOT_FOUND));
	}

	private void validatePaymentOwnerPermission(Payment payment, RequestUserDetails userDetails) {
		if (!payment.getUserId().equals(userDetails.getUserId()) && !hasMasterRole(userDetails)) {
			throw new CustomException(PaymentExceptionCode.UNAUTHORIZED);
		}
	}

	private boolean hasMasterRole(RequestUserDetails userDetails) {
		return userDetails.getAuthorities().stream()
			.anyMatch(auth -> auth.getAuthority().equals("ROLE_MASTER"));
	}
}
