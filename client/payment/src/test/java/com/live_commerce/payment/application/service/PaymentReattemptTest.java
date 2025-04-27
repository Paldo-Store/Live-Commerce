package com.live_commerce.payment.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.live_commerce.payment.application.dto.request.PaymentReadyRequestDto;
import com.live_commerce.payment.application.port.KakaoPayClient;
import com.live_commerce.payment.domain.model.Payment;
import com.live_commerce.payment.domain.repository.PaymentRepository;
import com.live_commerce.payment.infrastructure.client.dto.KakaoPayReadyDto;
import com.live_commerce.payment.infrastructure.security.RequestUserDetails;

@SpringBootTest
@ActiveProfiles("test")
public class PaymentReattemptTest {

	@Autowired
	private PaymentService paymentService;

	@Autowired
	private PaymentRepository paymentRepository;

	@MockitoBean
	private KakaoPayClient kakaoPayClient;

	@BeforeEach
	void setUp() {
		reset(kakaoPayClient);
	}

	@DisplayName("API 실패 후 동일 orderId로 재요청 시 결제 준비 성공")
	@Test
	void failedPayment_should_allow_repayment_on_same_orderId() {
		UUID userId = UUID.randomUUID();
		UUID orderId = UUID.randomUUID();
		RequestUserDetails user = new RequestUserDetails(userId, null, Collections.emptyList());
		PaymentReadyRequestDto dto = new PaymentReadyRequestDto(orderId, BigDecimal.valueOf(12000), "재결제 테스트");

		when(kakaoPayClient.requestKakaoPayReady(any(), any(), any(), any()))
			.thenThrow(new RuntimeException("카카오페이 실패"));

		try {
			paymentService.readyPayment(user, dto);
		} catch (Exception ignored) {}

		assertTrue(paymentRepository.findByOrderId(orderId).isEmpty());

		reset(kakaoPayClient);
		when(kakaoPayClient.requestKakaoPayReady(any(), any(), any(), any()))
			.thenReturn(new KakaoPayReadyDto("T_OK", "https://ok", "https://ok", "2025-04-19T00:00:00"));

		assertDoesNotThrow(() -> paymentService.readyPayment(user, dto));

		Payment saved = paymentRepository.findByOrderId(orderId).orElseThrow();
		assertEquals("PENDING", saved.getStatus().name());
		assertEquals("T_OK", saved.getTid());
	}
}