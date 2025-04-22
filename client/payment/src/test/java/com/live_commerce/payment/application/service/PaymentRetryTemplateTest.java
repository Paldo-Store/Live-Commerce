package com.live_commerce.payment.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestTemplate;

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
public class PaymentRetryTemplateTest {
	@Autowired
	private PaymentService paymentService;

	@Autowired
	private PaymentRepository paymentRepository;


	@Autowired
	private RedissonClient redissonClient;

	@MockitoBean
	private KakaoPayClient kakaoPayClient;
	@MockitoBean
	private PaymentSuccessEventProducer paymentSuccessEventProducer;
	@MockitoBean
	private PaymentCancelEventProducer paymentCancelEventProducer;
	@MockitoBean
	private RestTemplate restTemplate;

	@BeforeEach
	void setUp() {
		// 카카오페이 요청을 mocking (결제 준비 요청 시 고정된 응답을 반환)
		KakaoPayReadyDto mockDto = new KakaoPayReadyDto(
			"T123456789",
			"https://kakao.pay.pc",
			"https://kakao.pay.mobile",
			"2025-04-17T21:20:00"
		);

		when(kakaoPayClient.requestKakaoPayReady(any(), any(), any(), any()))
			.thenReturn(mockDto);
	}


	/**
	 * 외부 API 호출 실패 시 RetryTemplate을 통해 재시도가 정상 동작하는지 테스트
	 */
	@Test
	void retryTemplate_should_retry_on_failure_and_succeed() {
		UUID userId = UUID.randomUUID();
		UUID orderId = UUID.randomUUID();
		RequestUserDetails user = new RequestUserDetails(userId, null, Collections.emptyList());
		PaymentReadyRequestDto dto = new PaymentReadyRequestDto(orderId, BigDecimal.valueOf(10000), "RetryTestItem");

		KakaoPayReadyDto readyDto = new KakaoPayReadyDto("T_RETRY", "https://pay.pc", "https://pay.mobile", "2025-04-18T00:00:00");

		when(restTemplate.postForEntity(
			contains("https://open-api.kakaopay.com/online/v1/payment/ready"),
			any(),
			eq(KakaoPayReadyDto.class)
		)).thenThrow(new RuntimeException("1st failure"))
			.thenThrow(new RuntimeException("2nd failure"))
			.thenReturn(ResponseEntity.ok(readyDto));

		assertDoesNotThrow(() -> paymentService.readyPayment(user, dto));

		// 저장된 결제 확인
		Optional<Payment> saved = paymentRepository.findByOrderId(orderId);
		assertTrue(saved.isPresent(), "결제 정보가 저장되지 않았습니다");
		assertEquals(orderId, saved.get().getOrderId());
	}
}
