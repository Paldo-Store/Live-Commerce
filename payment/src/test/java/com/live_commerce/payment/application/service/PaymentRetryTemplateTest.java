package com.live_commerce.payment.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.live_commerce.payment.application.dto.request.PaymentReadyRequestDto;
import com.live_commerce.payment.application.port.dto.PaymentReadyResult;
import com.live_commerce.payment.domain.model.Payment;
import com.live_commerce.payment.domain.model.PaymentMethod;
import com.live_commerce.payment.domain.repository.PaymentRepository;
import com.live_commerce.payment.infrastructure.client.KakaoPayGateway;
import com.live_commerce.payment.infrastructure.security.RequestUserDetails;

@SpringBootTest
@ActiveProfiles("test")
public class PaymentRetryTemplateTest {

	@Autowired
	private PaymentService paymentService;

	@Autowired
	private PaymentRepository paymentRepository;

	@MockitoBean
	private KakaoPayGateway kakaoPayGateway;

	@BeforeEach
	void setUp() {
		when(kakaoPayGateway.ready(any(), any(), any(), any()))
			.thenReturn(new PaymentReadyResult("T123456789", "https://kakao.pay.pc"));
	}

	@DisplayName("카카오페이 Gateway 성공 - 결제 정보 저장")
	@Test
	void should_save_payment_when_gateway_succeeds() {
		UUID userId = UUID.randomUUID();
		UUID orderId = UUID.randomUUID();
		RequestUserDetails user = new RequestUserDetails(userId, null, Collections.emptyList());
		PaymentReadyRequestDto dto = new PaymentReadyRequestDto(orderId, BigDecimal.valueOf(10000), "RetryTestItem", PaymentMethod.KAKAO);

		assertDoesNotThrow(() -> paymentService.readyPayment(user, dto));

		Optional<Payment> saved = paymentRepository.findByOrderId(orderId);
		assertTrue(saved.isPresent(), "결제 정보가 저장되지 않았습니다");
		assertEquals(orderId, saved.get().getOrderId());
	}
}
