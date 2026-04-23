package com.live_commerce.payment.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestClientException;

import com.live_commerce.payment.application.dto.request.PaymentApproveRequestDto;
import com.live_commerce.payment.application.dto.response.PaymentApproveResponseDto;
import com.live_commerce.payment.application.dto.response.PaymentGetResponseDto;
import com.live_commerce.payment.application.dto.response.PaymentRefundResponseDto;
import com.live_commerce.payment.application.exception.CustomException;
import com.live_commerce.payment.application.exception.PaymentExceptionCode;
import com.live_commerce.payment.application.port.KakaoPayClient;
import com.live_commerce.payment.domain.model.Payment;
import com.live_commerce.payment.domain.model.PaymentStatus;
import com.live_commerce.payment.domain.repository.PaymentRepository;
import com.live_commerce.payment.domain.repository.PaymentSearchCondition;
import com.live_commerce.payment.infrastructure.client.OrderClient;
import com.live_commerce.payment.infrastructure.client.dto.KakaoPayApproveDto;
import com.live_commerce.payment.infrastructure.redis.PaymentRedisKeys;
import com.live_commerce.payment.infrastructure.security.RequestUserDetails;

@SpringBootTest
@ActiveProfiles("test")
public class PaymentServiceV2Test {

	@Autowired
	private PaymentServiceV2 paymentServiceV2;
	@Autowired
	private PaymentRepository paymentRepository;
	@Autowired
	private RedissonClient redissonClient;

	@MockitoBean
	private PaymentTxProcessor paymentTxProcessor;
	@MockitoBean
	private KakaoPayClient kakaoPayClient;
	@MockitoBean
	private OrderClient orderClient;

	private UUID userId;
	private UUID orderId;

	@BeforeEach
	void setup() {
		paymentRepository.deleteAll();
		userId = UUID.randomUUID();
		orderId = UUID.randomUUID();
		redissonClient.getBucket(PaymentRedisKeys.EXPIRE_KEY_PREFIX + orderId).delete();
	}

	// ── approvePayment ────────────────────────────────────────────────────

	@DisplayName("결제 승인 실패 - 결제 없음")
	@Test
	void approvePayment_notFound_fail() {
		PaymentApproveRequestDto dto = new PaymentApproveRequestDto("TID", "pgToken", orderId.toString());
		CustomException ex = assertThrows(CustomException.class, () ->
			paymentServiceV2.approvePayment(dto, userId)
		);
		assertEquals(PaymentExceptionCode.NOT_FOUND, ex.getExceptionCode());
	}

	@DisplayName("결제 승인 실패 - 상태가 PENDING이 아님")
	@Test
	void approvePayment_invalidStatus_fail() {
		Payment payment = Payment.of(userId, orderId, BigDecimal.valueOf(10000));
		payment.updateStatus(PaymentStatus.COMPLETED);
		paymentRepository.save(payment);
		PaymentApproveRequestDto dto = new PaymentApproveRequestDto("TID", "pgToken", orderId.toString());
		CustomException ex = assertThrows(CustomException.class, () ->
			paymentServiceV2.approvePayment(dto, userId)
		);
		assertEquals(PaymentExceptionCode.INVALID_STATUS, ex.getExceptionCode());
	}

	@DisplayName("결제 승인 실패 - Redis 만료 키 없음")
	@Test
	void approvePayment_paymentExpired_fail() {
		Payment payment = Payment.of(userId, orderId, BigDecimal.valueOf(10000));
		paymentRepository.save(payment);
		// Redis key 없는 상태 유지 (setup에서 삭제됨)
		PaymentApproveRequestDto dto = new PaymentApproveRequestDto("TID", "pgToken", orderId.toString());
		CustomException ex = assertThrows(CustomException.class, () ->
			paymentServiceV2.approvePayment(dto, userId)
		);
		assertEquals(PaymentExceptionCode.PAYMENT_EXPIRED, ex.getExceptionCode());
	}

	@DisplayName("결제 승인 실패 - 카카오 API 실패 시 paymentTxProcessor.fail() 호출")
	@Test
	void approvePayment_kakaoApiFail_callsTxFail() {
		Payment payment = Payment.of(userId, orderId, BigDecimal.valueOf(10000));
		paymentRepository.save(payment);
		setRedisExpireKey(orderId);
		when(kakaoPayClient.requestKakaoPayApprove(any(), any(), any(), any()))
			.thenThrow(new RestClientException("카카오 오류"));
		PaymentApproveRequestDto dto = new PaymentApproveRequestDto("TID", "pgToken", orderId.toString());
		CustomException ex = assertThrows(CustomException.class, () ->
			paymentServiceV2.approvePayment(dto, userId)
		);
		assertEquals(PaymentExceptionCode.PAYMENT_APPROVE_FAIL, ex.getExceptionCode());
		verify(paymentTxProcessor).fail(orderId, "카카오페이 승인 실패");
	}

	@DisplayName("결제 승인 실패 - DB 업데이트 실패 시 카카오 보상 취소 호출")
	@Test
	void approvePayment_dbUpdateFail_callsKakaoCompensation() {
		Payment payment = Payment.of(userId, orderId, BigDecimal.valueOf(10000));
		payment.assignTid("TID");
		paymentRepository.save(payment);
		setRedisExpireKey(orderId);
		KakaoPayApproveDto approveDto = buildKakaoApproveDto(orderId, userId, 10000);
		when(kakaoPayClient.requestKakaoPayApprove(any(), any(), any(), any())).thenReturn(approveDto);
		doThrow(new RuntimeException("DB 오류")).when(paymentTxProcessor).complete(orderId);
		PaymentApproveRequestDto dto = new PaymentApproveRequestDto("TID", "pgToken", orderId.toString());
		CustomException ex = assertThrows(CustomException.class, () ->
			paymentServiceV2.approvePayment(dto, userId)
		);
		assertEquals(PaymentExceptionCode.PAYMENT_APPROVE_FAIL, ex.getExceptionCode());
		verify(kakaoPayClient).requestKakaoPayCancel(eq("TID"), any());
	}

	@DisplayName("결제 승인 성공 - complete 호출 및 Redis 키 삭제")
	@Test
	void approvePayment_success() {
		Payment payment = Payment.of(userId, orderId, BigDecimal.valueOf(10000));
		paymentRepository.save(payment);
		setRedisExpireKey(orderId);
		when(kakaoPayClient.requestKakaoPayApprove(any(), any(), any(), any()))
			.thenReturn(buildKakaoApproveDto(orderId, userId, 10000));
		PaymentApproveRequestDto dto = new PaymentApproveRequestDto("TID", "pgToken", orderId.toString());
		PaymentApproveResponseDto result = paymentServiceV2.approvePayment(dto, userId);
		verify(paymentTxProcessor).complete(orderId);
		assertFalse(redissonClient.getBucket(PaymentRedisKeys.EXPIRE_KEY_PREFIX + orderId).isExists());
		assertNotNull(result);
	}

	// ── refundPaymentByOrderId ────────────────────────────────────────────

	@DisplayName("결제 환불 실패 - 상태가 COMPLETED 아님")
	@Test
	void refundPayment_notCompleted_fail() {
		Payment payment = Payment.of(userId, orderId, BigDecimal.valueOf(8000));
		paymentRepository.save(payment); // PENDING
		RequestUserDetails userDetails = new RequestUserDetails(userId, null, Collections.emptyList());
		CustomException ex = assertThrows(CustomException.class, () ->
			paymentServiceV2.refundPaymentByOrderId(orderId, userDetails)
		);
		assertEquals(PaymentExceptionCode.INVALID_STATUS, ex.getExceptionCode());
		verify(kakaoPayClient, never()).requestKakaoPayCancel(any(), any());
	}

	@DisplayName("결제 환불 실패 - 권한 없음")
	@Test
	void refundPayment_unauthorized_fail() {
		Payment payment = Payment.of(userId, orderId, BigDecimal.valueOf(8000));
		payment.updateStatus(PaymentStatus.COMPLETED);
		paymentRepository.save(payment);
		RequestUserDetails otherUser = new RequestUserDetails(UUID.randomUUID(), null, Collections.emptyList());
		CustomException ex = assertThrows(CustomException.class, () ->
			paymentServiceV2.refundPaymentByOrderId(orderId, otherUser)
		);
		assertEquals(PaymentExceptionCode.UNAUTHORIZED, ex.getExceptionCode());
	}

	@DisplayName("결제 환불 실패 - 카카오 취소 API 실패 시 DB 업데이트 하지 않음")
	@Test
	void refundPayment_kakaoFail_noDbUpdate() {
		Payment payment = Payment.of(userId, orderId, BigDecimal.valueOf(8000));
		payment.updateStatus(PaymentStatus.COMPLETED);
		payment.assignTid("TID_REF");
		paymentRepository.save(payment);
		RequestUserDetails userDetails = new RequestUserDetails(userId, null, Collections.emptyList());
		when(kakaoPayClient.requestKakaoPayCancel(any(), any())).thenThrow(new RestClientException("실패"));
		CustomException ex = assertThrows(CustomException.class, () ->
			paymentServiceV2.refundPaymentByOrderId(orderId, userDetails)
		);
		assertEquals(PaymentExceptionCode.PAYMENT_REFUND_FAIL, ex.getExceptionCode());
		verify(paymentTxProcessor, never()).refund(any());
	}

	@DisplayName("결제 환불 성공 - refund 호출 및 주문 알림 전송")
	@Test
	void refundPayment_success() {
		Payment payment = Payment.of(userId, orderId, BigDecimal.valueOf(8000));
		payment.updateStatus(PaymentStatus.COMPLETED);
		payment.assignTid("TID_REF");
		paymentRepository.save(payment);
		RequestUserDetails userDetails = new RequestUserDetails(userId, null, Collections.emptyList());
		Payment refunded = Payment.of(userId, orderId, BigDecimal.valueOf(8000));
		refunded.updateStatus(PaymentStatus.REFUND);
		when(paymentTxProcessor.refund(orderId)).thenReturn(refunded);
		PaymentRefundResponseDto result = paymentServiceV2.refundPaymentByOrderId(orderId, userDetails);
		verify(paymentTxProcessor).refund(orderId);
		verify(orderClient).notifyOrderCancel(eq(orderId), any());
		assertNotNull(result);
		assertEquals(orderId, result.orderId());
	}

	@DisplayName("결제 환불 성공 - 마스터가 다른 유저 결제 환불")
	@Test
	void refundPayment_byMaster_success() {
		Payment payment = Payment.of(userId, orderId, BigDecimal.valueOf(12000));
		payment.updateStatus(PaymentStatus.COMPLETED);
		payment.assignTid("TID_MASTER");
		paymentRepository.save(payment);
		RequestUserDetails master = new RequestUserDetails(UUID.randomUUID(), null, List.of(() -> "ROLE_MASTER"));
		Payment refunded = Payment.of(userId, orderId, BigDecimal.valueOf(12000));
		refunded.updateStatus(PaymentStatus.REFUND);
		when(paymentTxProcessor.refund(orderId)).thenReturn(refunded);
		PaymentRefundResponseDto result = paymentServiceV2.refundPaymentByOrderId(orderId, master);
		verify(paymentTxProcessor).refund(orderId);
		assertEquals(orderId, result.orderId());
	}

	// ── cancelPaymentByOrderId ────────────────────────────────────────────

	@DisplayName("결제 취소 실패 - 상태가 PENDING 아님")
	@Test
	void cancelPayment_notPending_fail() {
		Payment payment = Payment.of(userId, orderId, BigDecimal.valueOf(5000));
		payment.updateStatus(PaymentStatus.COMPLETED);
		paymentRepository.save(payment);
		RequestUserDetails userDetails = new RequestUserDetails(userId, null, Collections.emptyList());
		CustomException ex = assertThrows(CustomException.class, () ->
			paymentServiceV2.cancelPaymentByOrderId(orderId, userDetails)
		);
		assertEquals(PaymentExceptionCode.INVALID_STATUS, ex.getExceptionCode());
		verify(paymentTxProcessor, never()).cancel(any());
	}

	@DisplayName("결제 취소 실패 - 권한 없음")
	@Test
	void cancelPayment_unauthorized_fail() {
		Payment payment = Payment.of(userId, orderId, BigDecimal.valueOf(5000));
		paymentRepository.save(payment);
		RequestUserDetails otherUser = new RequestUserDetails(UUID.randomUUID(), null, Collections.emptyList());
		CustomException ex = assertThrows(CustomException.class, () ->
			paymentServiceV2.cancelPaymentByOrderId(orderId, otherUser)
		);
		assertEquals(PaymentExceptionCode.UNAUTHORIZED, ex.getExceptionCode());
	}

	@DisplayName("결제 취소 성공 - cancel 호출 및 주문 알림 전송")
	@Test
	void cancelPayment_success() {
		Payment payment = Payment.of(userId, orderId, BigDecimal.valueOf(5000));
		paymentRepository.save(payment);
		RequestUserDetails userDetails = new RequestUserDetails(userId, null, Collections.emptyList());
		paymentServiceV2.cancelPaymentByOrderId(orderId, userDetails);
		verify(paymentTxProcessor).cancel(orderId);
		verify(orderClient).notifyOrderCancel(eq(orderId), any());
	}

	@DisplayName("결제 취소 성공 - 마스터가 다른 유저 결제 취소")
	@Test
	void cancelPayment_byMaster_success() {
		Payment payment = Payment.of(userId, orderId, BigDecimal.valueOf(5000));
		paymentRepository.save(payment);
		RequestUserDetails master = new RequestUserDetails(UUID.randomUUID(), null, List.of(() -> "ROLE_MASTER"));
		paymentServiceV2.cancelPaymentByOrderId(orderId, master);
		verify(paymentTxProcessor).cancel(orderId);
	}

	// ── compensateRefundByOrderId ─────────────────────────────────────────

	@DisplayName("보상 환불 - COMPLETED 아니면 카카오 취소 호출 없음 (early return)")
	@Test
	void compensateRefund_notCompleted_skip() {
		Payment payment = Payment.of(userId, orderId, BigDecimal.valueOf(5000)); // PENDING
		paymentRepository.save(payment);
		paymentServiceV2.compensateRefundByOrderId(orderId, "테스트");
		verify(kakaoPayClient, never()).requestKakaoPayCancel(any(), any());
		verify(paymentTxProcessor, never()).refund(any());
	}

	@DisplayName("보상 환불 - COMPLETED 상태면 카카오 취소 후 refund 호출")
	@Test
	void compensateRefund_completed_success() {
		Payment payment = Payment.of(userId, orderId, BigDecimal.valueOf(5000));
		payment.updateStatus(PaymentStatus.COMPLETED);
		payment.assignTid("TID_COMP");
		paymentRepository.save(payment);
		when(paymentTxProcessor.refund(orderId)).thenReturn(payment);
		paymentServiceV2.compensateRefundByOrderId(orderId, "보상 처리");
		verify(kakaoPayClient).requestKakaoPayCancel(eq("TID_COMP"), any());
		verify(paymentTxProcessor).refund(orderId);
	}

	// ── getPayments ───────────────────────────────────────────────────────

	@DisplayName("결제 목록 조회 - 비정상 페이지 크기(99)는 10으로 보정")
	@Test
	void getPayments_invalidPageSize_defaultsTo10() {
		for (int i = 0; i < 12; i++) {
			paymentRepository.save(Payment.of(userId, UUID.randomUUID(), BigDecimal.valueOf(1000)));
		}
		RequestUserDetails userDetails = new RequestUserDetails(userId, null, Collections.emptyList());
		Page<PaymentGetResponseDto> result = paymentServiceV2.getPayments(
			new PaymentSearchCondition(null, null, null, null, null),
			userDetails, PageRequest.of(0, 99)
		);
		assertEquals(10, result.getContent().size());
		assertEquals(12, result.getTotalElements());
	}

	@DisplayName("결제 목록 조회 - 소유자는 본인 결제만 조회")
	@Test
	void getPayments_owner_seesOnlyOwn() {
		for (int i = 0; i < 3; i++) {
			paymentRepository.save(Payment.of(userId, UUID.randomUUID(), BigDecimal.valueOf(1000)));
		}
		for (int i = 0; i < 2; i++) {
			paymentRepository.save(Payment.of(UUID.randomUUID(), UUID.randomUUID(), BigDecimal.valueOf(2000)));
		}
		RequestUserDetails userDetails = new RequestUserDetails(userId, null, Collections.emptyList());
		Page<PaymentGetResponseDto> result = paymentServiceV2.getPayments(
			new PaymentSearchCondition(null, null, null, null, null),
			userDetails, PageRequest.of(0, 10)
		);
		assertEquals(3, result.getTotalElements());
	}

	@DisplayName("결제 목록 조회 - 마스터는 전체 결제 조회")
	@Test
	void getPayments_master_seesAll() {
		for (int i = 0; i < 5; i++) {
			paymentRepository.save(Payment.of(UUID.randomUUID(), UUID.randomUUID(), BigDecimal.valueOf(1000)));
		}
		RequestUserDetails master = new RequestUserDetails(UUID.randomUUID(), null, List.of(() -> "ROLE_MASTER"));
		Page<PaymentGetResponseDto> result = paymentServiceV2.getPayments(
			new PaymentSearchCondition(null, null, null, null, null),
			master, PageRequest.of(0, 10)
		);
		assertEquals(5, result.getTotalElements());
	}

	// ── helpers ──────────────────────────────────────────────────────────

	private void setRedisExpireKey(UUID orderId) {
		redissonClient.getBucket(PaymentRedisKeys.EXPIRE_KEY_PREFIX + orderId).set("1");
	}

	private KakaoPayApproveDto buildKakaoApproveDto(UUID orderId, UUID userId, int amount) {
		return new KakaoPayApproveDto(
			"aid", "TID", "cid",
			orderId.toString(), userId.toString(),
			"MONEY",
			new KakaoPayApproveDto.Amount(amount, 0, 0, 0, 0),
			"2024-01-01T00:00:00"
		);
	}
}
