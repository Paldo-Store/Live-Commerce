package com.live_commerce.payment.application.service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.live_commerce.payment.application.dto.request.PaymentApproveRequestDto;
import com.live_commerce.payment.application.dto.request.PaymentReadyRequestDto;
import com.live_commerce.payment.application.dto.request.PaymentRefundResponseDto;
import com.live_commerce.payment.application.dto.request.PaymentSearchCondition;
import com.live_commerce.payment.application.dto.response.PaymentApproveResponseDto;
import com.live_commerce.payment.application.dto.response.PaymentGetResponseDto;
import com.live_commerce.payment.application.dto.response.PaymentReadyResponseDto;
import com.live_commerce.payment.application.exception.CustomException;
import com.live_commerce.payment.application.exception.PaymentExceptionCode;
import com.live_commerce.payment.application.port.KakaoPayClient;
import com.live_commerce.payment.domain.model.Payment;
import com.live_commerce.payment.domain.model.PaymentStatus;
import com.live_commerce.payment.domain.repository.PaymentRepository;
import com.live_commerce.payment.infrastructure.client.OrderClient;
import com.live_commerce.payment.infrastructure.client.dto.KakaoPayApproveDto;
import com.live_commerce.payment.infrastructure.client.dto.KakaoPayReadyDto;
import com.live_commerce.payment.infrastructure.client.dto.PaymentCancelRequest;
import com.live_commerce.payment.infrastructure.kafka.dto.PaymentCompletedEvent;
import com.live_commerce.payment.infrastructure.kafka.dto.PaymentFailedEvent;
import com.live_commerce.payment.infrastructure.kafka.producer.PaymentEventProducer;
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
	private final PaymentEventProducer paymentEventProducer;

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

		String key = "payment:expire:" + dto.orderId();
		RBucket<String> bucket = redissonClient.getBucket(key);
		bucket.set(payment.getId().toString(), 10, TimeUnit.MINUTES);

		return PaymentReadyResponseDto.from(readyDto);
	}

	@Transactional
	public PaymentApproveResponseDto approvePayment(PaymentApproveRequestDto requestDto, UUID userId) {
		Payment payment = paymentRepository.findByOrderId(UUID.fromString(requestDto.orderId()))
			.orElseThrow(() -> new CustomException(PaymentExceptionCode.NOT_FOUND));

		if (payment.getStatus() != PaymentStatus.PENDING) {
			payment.updateStatus(PaymentStatus.FAILED);
			throw new CustomException(PaymentExceptionCode.INVALID_STATUS);
		}

		KakaoPayApproveDto approveDto;
		try {
			approveDto = kakaoPayClient.requestKakaoPayApprove(
				requestDto.tid(), requestDto.pgToken(), requestDto.orderId(), userId.toString()
			);
		} catch (Exception e) {
			payment.updateStatus(PaymentStatus.FAILED);

			// 카프카로 결제 실패 이벤트 발행
			paymentEventProducer.sendPaymentFailed(
				new PaymentFailedEvent(payment.getOrderId(), "카카오페이 승인 실패")
			);

			throw new CustomException(PaymentExceptionCode.PAYMENT_APPROVE_FAIL);
		}

		payment.updateStatus(PaymentStatus.COMPLETED);

		// 카프카로 결제 완료 이벤트 발행
		paymentEventProducer.sendPaymentCompleted(
			new PaymentCompletedEvent(
				payment.getOrderId(),
				"결제 완료",
				payment.getAmount()
			)
		);


		return PaymentApproveResponseDto.from(approveDto);
	}

	@Transactional(readOnly = true)
	public PaymentGetResponseDto getPayment(UUID paymentId, RequestUserDetails userDetails) {
		Payment payment = findPaymentById(paymentId);
		validatePaymentGetPermission(payment, userDetails);

		return PaymentGetResponseDto.from(payment);
	}

	@Transactional(readOnly = true)
	public Page<PaymentGetResponseDto> getPayments(
		PaymentSearchCondition condition,
		RequestUserDetails userDetails,
		Pageable pageable
	) {
		validatePaymentSearchPermission(userDetails);

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

		List<PaymentGetResponseDto> dtoList = payments.stream()
			.map(PaymentGetResponseDto::from)
			.toList();

		return new PageImpl<>(dtoList, pageable, totalCount);
	}

	@Transactional
	public PaymentRefundResponseDto refundPaymentByOrderId(UUID orderId, RequestUserDetails userDetails) {
		Payment payment = findPaymentByOrderId(orderId);
		validatePaymentRefundPermission(payment, userDetails);

		if (payment.getStatus() != PaymentStatus.COMPLETED) {
			throw new CustomException(PaymentExceptionCode.INVALID_STATUS);
		}

		kakaoPayClient.requestKakaoPayCancel(payment.getTid(), payment.getAmount());
		payment.updateStatus(PaymentStatus.REFUND);

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
		validatePaymentCancelPermission(payment, userDetails);

		if (payment.getStatus() != PaymentStatus.PENDING) {
			throw new CustomException(PaymentExceptionCode.INVALID_STATUS);
		}

		try {
			orderClient.notifyOrderCancel(orderId, new PaymentCancelRequest(false, "결제 취소 처리됨"));
		} catch (Exception e) {
			log.warn("주문 서비스에 결제 취소 알림 실패: {}", e.getMessage());
		}

		payment.updateStatus(PaymentStatus.CANCELED);
	}

	@Transactional
	public void compensateRefundByOrderId(UUID orderId, String message) {
		Payment payment = paymentRepository.findByOrderId(orderId)
			.orElseThrow(() -> new CustomException(PaymentExceptionCode.NOT_FOUND));

		if (payment.getStatus() != PaymentStatus.COMPLETED) {
			log.info("[Payment] 보상 처리 스킵: 이미 취소/실패한 결제입니다. orderId={}, status={}", orderId, payment.getStatus());
			return;
		}

		kakaoPayClient.requestKakaoPayCancel(payment.getTid(), payment.getAmount());
		payment.updateStatus(PaymentStatus.REFUND);

		log.info("[Payment] 보상 결제 취소 완료: orderId = {}, message = {}", orderId, message);
	}


	private Payment findPaymentById(UUID paymentId) {
		return paymentRepository.findById(paymentId)
			.orElseThrow(() -> new CustomException(PaymentExceptionCode.NOT_FOUND));
	}

	private Payment findPaymentByOrderId(UUID orderId) {
		return paymentRepository.findByOrderId(orderId)
			.orElseThrow(() -> new CustomException(PaymentExceptionCode.NOT_FOUND));
	}

	private void validatePaymentGetPermission(Payment payment, RequestUserDetails userDetails) {
		if (!payment.getUserId().equals(userDetails.getUserId()) && !hasMasterRole(userDetails)) {
			throw new CustomException(PaymentExceptionCode.UNAUTHORIZED);
		}
	}

	private void validatePaymentSearchPermission(RequestUserDetails userDetails) {
		if (!isSelf(userDetails.getUserId(), userDetails) && !hasMasterRole(userDetails)) {
			throw new CustomException(PaymentExceptionCode.UNAUTHORIZED);
		}
	}

	private void validatePaymentRefundPermission(Payment payment, RequestUserDetails userDetails) {
		if (!payment.getUserId().equals(userDetails.getUserId()) && !hasMasterRole(userDetails)) {
			throw new CustomException(PaymentExceptionCode.UNAUTHORIZED);
		}
	}

	private void validatePaymentCancelPermission(Payment payment, RequestUserDetails userDetails) {
		if (!payment.getUserId().equals(userDetails.getUserId()) && !hasMasterRole(userDetails)) {
			throw new CustomException(PaymentExceptionCode.UNAUTHORIZED);
		}
	}

	private boolean isSelf(UUID userId, RequestUserDetails userDetails) {
		return userId.equals(userDetails.getUserId());
	}

	private boolean hasMasterRole(RequestUserDetails userDetails) {
		return userDetails.getAuthorities().stream()
			.anyMatch(auth -> auth.getAuthority().equals("ROLE_MASTER"));
	}
}
