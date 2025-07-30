package com.live_commerce.payment.kakao;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestTemplate;

import com.live_commerce.payment.infrastructure.client.KakaoPayClientImpl;
import com.live_commerce.payment.infrastructure.client.dto.KakaoPayReadyDto;

@ExtendWith(MockitoExtension.class)
class KakaoPayClientTest {

	@Mock
	private RestTemplate restTemplate;

	private KakaoPayClientImpl kakaoPayClient;

	@BeforeEach
	void setUp() {
		RetryTemplate retryTemplate = RetryTemplate.builder()
			.maxAttempts(3)
			.fixedBackoff(1000)
			.build();

		kakaoPayClient = new KakaoPayClientImpl(restTemplate, retryTemplate);
	}

	@Test
	void testRetry() {
		UUID userId = UUID.randomUUID();
		UUID orderId = UUID.randomUUID();
		BigDecimal amount = BigDecimal.valueOf(1000);

		KakaoPayReadyDto readyDto = new KakaoPayReadyDto(
			"dummy-tid",
			"https://example.com/pc",
			"https://example.com/mobile",
			"2024-04-13T14:30:00"
		);

		when(restTemplate.postForEntity(anyString(), any(), eq(KakaoPayReadyDto.class)))
			.thenThrow(new RuntimeException("1st fail"))
			.thenThrow(new RuntimeException("2nd fail"))
			.thenReturn(new ResponseEntity<>(readyDto, HttpStatus.OK));

		KakaoPayReadyDto result = kakaoPayClient.requestKakaoPayReady(userId, orderId, amount, "테스트");

		assertEquals(readyDto, result);
		verify(restTemplate, times(3)).postForEntity(anyString(), any(), eq(KakaoPayReadyDto.class));
	}
}
