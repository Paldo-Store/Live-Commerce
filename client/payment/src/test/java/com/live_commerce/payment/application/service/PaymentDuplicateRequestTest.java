package com.live_commerce.payment.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
import com.live_commerce.payment.infrastructure.kafka.producer.PaymentCancelEventProducer;
import com.live_commerce.payment.infrastructure.kafka.producer.PaymentSuccessEventProducer;
import com.live_commerce.payment.infrastructure.security.RequestUserDetails;

@SpringBootTest
@ActiveProfiles("test")
public class PaymentDuplicateRequestTest {

	@Autowired
	private PaymentService paymentService;

	@Autowired
	private PaymentRepository paymentRepository;

	@MockitoBean
	private KakaoPayClient kakaoPayClient;
	@MockitoBean
	private PaymentSuccessEventProducer paymentSuccessEventProducer;
	@MockitoBean
	private PaymentCancelEventProducer paymentCancelEventProducer;

	@BeforeEach
	void setUp() {
		KakaoPayReadyDto mockDto = new KakaoPayReadyDto(
			"T123456789",
			"https://kakao.pay.pc",
			"https://kakao.pay.mobile",
			"2025-04-17T21:20:00"
		);
		when(kakaoPayClient.requestKakaoPayReady(any(), any(), any(), any()))
			.thenReturn(mockDto);
	}

	@DisplayName("동시 결제 요청 시 중복 결제 방지 테스트")
	@Test
	void duplicatePaymentRequest_should_create_only_one_payment() throws Exception {
		UUID userId = UUID.randomUUID();
		UUID orderId = UUID.randomUUID();
		PaymentReadyRequestDto dto = new PaymentReadyRequestDto(orderId, BigDecimal.valueOf(9999), "중복 테스트");
		RequestUserDetails user = new RequestUserDetails(userId, null, Collections.emptyList());

		int threadCount = 5;
		CountDownLatch latch = new CountDownLatch(threadCount);
		ExecutorService executor = Executors.newFixedThreadPool(threadCount);

		for (int i = 0; i < threadCount; i++) {
			executor.submit(() -> {
				try {
					paymentService.readyPayment(user, dto);
				} catch (Exception ignored) {}
				finally {
					latch.countDown();
				}
			});
		}

		latch.await();

		long count = paymentRepository.findAll().stream()
			.filter(p -> p.getOrderId().equals(orderId))
			.count();

		assertEquals(1, count, "중복 결제가 생성되면 안됩니다.");
	}
}
