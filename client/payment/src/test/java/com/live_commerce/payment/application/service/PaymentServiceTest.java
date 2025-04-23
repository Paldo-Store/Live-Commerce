package com.live_commerce.payment.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import com.live_commerce.payment.application.dto.request.PaymentRefundResponseDto;
import com.live_commerce.payment.application.dto.request.PaymentSearchCondition;
import com.live_commerce.payment.application.dto.response.PaymentGetResponseDto;
import com.live_commerce.payment.application.exception.CustomException;
import com.live_commerce.payment.application.exception.PaymentExceptionCode;
import com.live_commerce.payment.application.port.KakaoPayClient;
import com.live_commerce.payment.domain.model.Payment;
import com.live_commerce.payment.domain.model.PaymentStatus;
import com.live_commerce.payment.domain.repository.PaymentRepository;
import com.live_commerce.payment.infrastructure.client.OrderClient;
import com.live_commerce.payment.infrastructure.kafka.producer.PaymentCancelEventProducer;
import com.live_commerce.payment.infrastructure.kafka.producer.PaymentSuccessEventProducer;
import com.live_commerce.payment.infrastructure.security.RequestUserDetails;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class PaymentServiceTest {

	@Autowired private PaymentService paymentService;
	@Autowired private PaymentRepository paymentRepository;
	@Autowired private RedissonClient redissonClient;

	@MockitoBean private OrderClient orderClient;
	@MockitoBean private PaymentSuccessEventProducer paymentSuccessEventProducer;
	@MockitoBean private PaymentCancelEventProducer paymentCancelEventProducer;
	@MockitoBean private KakaoPayClient kakaoPayClient;

	private UUID userId;
	private UUID orderId;

	@BeforeEach
	void setup() {
		paymentRepository.deleteAll();
		userId = UUID.randomUUID();
		orderId = UUID.randomUUID();
	}

	// 1. 단건 조회
	@Test
	void getPayment_should_succeed_for_owner() {
		// Given
		Payment payment = Payment.of(userId, orderId, BigDecimal.valueOf(7000));
		paymentRepository.save(payment);
		RequestUserDetails userDetails = new RequestUserDetails(userId, null, Collections.emptyList());
		// When
		var result = paymentService.getPayment(payment.getId(), userDetails);
		// Then
		assertEquals(payment.getId(), result.paymentId());
		assertEquals(orderId, result.orderId());
	}

	@Test
	void getPayment_should_succeed_for_master() {
		// Given
		Payment payment = Payment.of(userId, orderId, BigDecimal.valueOf(7000));
		paymentRepository.save(payment);
		RequestUserDetails master = new RequestUserDetails(UUID.randomUUID(), null, List.of(() -> "ROLE_MASTER"));
		// When
		var result = paymentService.getPayment(payment.getId(), master);
		// Then
		assertEquals(payment.getId(), result.paymentId());
	}

	@Test
	void getPayment_should_throw_if_not_owner_or_master() {
		// Given
		Payment payment = Payment.of(userId, orderId, BigDecimal.valueOf(5000));
		paymentRepository.save(payment);
		RequestUserDetails otherUser = new RequestUserDetails(UUID.randomUUID(), null, Collections.emptyList());
		// When & Then
		CustomException ex = assertThrows(CustomException.class, () -> {
			paymentService.getPayment(payment.getId(), otherUser);
		});
		assertEquals(PaymentExceptionCode.UNAUTHORIZED, ex.getExceptionCode());
	}

	// 2. 전체 조회
	@Test
	void getPayments_should_return_paginated_result_for_owner() {
		// Given
		for (int i = 0; i < 12; i++) {
			paymentRepository.save(Payment.of(userId, UUID.randomUUID(), BigDecimal.valueOf(1000 + i)));
		}
		RequestUserDetails userDetails = new RequestUserDetails(userId, null, Collections.emptyList());
		PageRequest pageable = PageRequest.of(0, 10);
		// When
		Page<PaymentGetResponseDto> result = paymentService.getPayments(
			new PaymentSearchCondition(null, null, null, null, null),
			userDetails,
			pageable
		);
		// Then
		assertEquals(10, result.getContent().size());
		assertEquals(12, result.getTotalElements());
	}

	@Test
	void getPayments_should_return_all_for_master() {
		// Given
		for (int i = 0; i < 5; i++) {
			paymentRepository.save(Payment.of(UUID.randomUUID(), UUID.randomUUID(), BigDecimal.valueOf(1000 + i)));
		}
		RequestUserDetails master = new RequestUserDetails(UUID.randomUUID(), null, List.of(() -> "ROLE_MASTER"));
		PageRequest pageable = PageRequest.of(0, 10);
		// When
		Page<PaymentGetResponseDto> result = paymentService.getPayments(
			new PaymentSearchCondition(null, null, null, null, null),
			master,
			pageable
		);
		// Then
		assertEquals(5, result.getContent().size());
	}

	// 3. 결제 취소
	@Test
	void cancelPayment_should_succeed_when_status_is_pending() {
		// Given
		Payment payment = Payment.of(userId, orderId, BigDecimal.valueOf(8000));
		paymentRepository.save(payment);
		RequestUserDetails userDetails = new RequestUserDetails(userId, null, Collections.emptyList());
		// When
		paymentService.cancelPaymentByOrderId(orderId, userDetails);
		// Then
		Payment updated = paymentRepository.findByOrderId(orderId).orElseThrow();
		assertEquals(PaymentStatus.CANCELED, updated.getStatus());
	}

	@Test
	void cancelPayment_should_succeed_for_master_on_others_payment() {
		// Given
		Payment payment = Payment.of(userId, orderId, BigDecimal.valueOf(8888));
		paymentRepository.save(payment);
		RequestUserDetails master = new RequestUserDetails(UUID.randomUUID(), null, List.of(() -> "ROLE_MASTER"));
		// When
		paymentService.cancelPaymentByOrderId(orderId, master);
		// Then
		Payment updated = paymentRepository.findByOrderId(orderId).orElseThrow();
		assertEquals(PaymentStatus.CANCELED, updated.getStatus());
	}

	@Test
	void cancelPayment_should_fail_if_already_completed() {
		// Given
		Payment payment = Payment.of(userId, orderId, BigDecimal.valueOf(15000));
		payment.updateStatus(PaymentStatus.COMPLETED);
		paymentRepository.save(payment);
		RequestUserDetails userDetails = new RequestUserDetails(userId, null, Collections.emptyList());
		// When & Then
		CustomException ex = assertThrows(CustomException.class, () -> {
			paymentService.cancelPaymentByOrderId(orderId, userDetails);
		});
		assertEquals(PaymentExceptionCode.INVALID_STATUS, ex.getExceptionCode());
	}

	@Test
	void cancelPayment_should_throw_if_not_owner_and_not_master() {
		// Given
		Payment payment = Payment.of(userId, orderId, BigDecimal.valueOf(5000));
		paymentRepository.save(payment);
		RequestUserDetails otherUser = new RequestUserDetails(UUID.randomUUID(), null, Collections.emptyList());
		// When & Then
		CustomException ex = assertThrows(CustomException.class, () -> {
			paymentService.cancelPaymentByOrderId(orderId, otherUser);
		});
		assertEquals(PaymentExceptionCode.UNAUTHORIZED, ex.getExceptionCode());
	}

	// 4. 결제 환불
	@Test
	void refundPayment_should_succeed_if_completed() {
		// Given
		Payment payment = Payment.of(userId, orderId, BigDecimal.valueOf(15000));
		payment.updateStatus(PaymentStatus.COMPLETED);
		payment.assignTid("TID123");
		paymentRepository.save(payment);
		RequestUserDetails userDetails = new RequestUserDetails(userId, null, Collections.emptyList());
		when(kakaoPayClient.requestKakaoPayCancel(any(), any())).thenReturn(null);
		// When
		PaymentRefundResponseDto response = paymentService.refundPaymentByOrderId(orderId, userDetails);
		// Then
		assertEquals(PaymentStatus.REFUND, paymentRepository.findByOrderId(orderId).get().getStatus());
		assertEquals(orderId, response.orderId());
	}

	@Test
	void refundPayment_should_succeed_for_master_on_others_payment() {
		// Given
		Payment payment = Payment.of(userId, orderId, BigDecimal.valueOf(20000));
		payment.updateStatus(PaymentStatus.COMPLETED);
		payment.assignTid("TID999");
		paymentRepository.save(payment);
		RequestUserDetails master = new RequestUserDetails(UUID.randomUUID(), null, List.of(() -> "ROLE_MASTER"));
		when(kakaoPayClient.requestKakaoPayCancel(any(), any())).thenReturn(null);
		// When
		var result = paymentService.refundPaymentByOrderId(orderId, master);
		// Then
		assertEquals(PaymentStatus.REFUND, paymentRepository.findByOrderId(orderId).get().getStatus());
		assertEquals(orderId, result.orderId());
	}

	@Test
	void refundPayment_should_fail_if_not_completed() {
		// Given
		Payment payment = Payment.of(userId, orderId, BigDecimal.valueOf(15000));
		payment.updateStatus(PaymentStatus.PENDING);
		paymentRepository.save(payment);
		RequestUserDetails userDetails = new RequestUserDetails(userId, null, Collections.emptyList());
		// When & Then
		CustomException ex = assertThrows(CustomException.class, () -> {
			paymentService.refundPaymentByOrderId(orderId, userDetails);
		});
		assertEquals(PaymentExceptionCode.INVALID_STATUS, ex.getExceptionCode());
	}

	@Test
	void refundPayment_should_throw_if_not_owner_and_not_master() {
		// Given
		Payment payment = Payment.of(userId, orderId, BigDecimal.valueOf(9999));
		payment.updateStatus(PaymentStatus.COMPLETED);
		payment.assignTid("TID456");
		paymentRepository.save(payment);
		RequestUserDetails otherUser = new RequestUserDetails(UUID.randomUUID(), null, Collections.emptyList());
		// When & Then
		CustomException ex = assertThrows(CustomException.class, () -> {
			paymentService.refundPaymentByOrderId(orderId, otherUser);
		});
		assertEquals(PaymentExceptionCode.UNAUTHORIZED, ex.getExceptionCode());
	}
}
