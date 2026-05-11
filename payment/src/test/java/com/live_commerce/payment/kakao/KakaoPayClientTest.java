package com.live_commerce.payment.kakao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import com.live_commerce.payment.application.port.dto.PaymentReadyResult;
import com.live_commerce.payment.infrastructure.client.KakaoPayGateway;
import com.live_commerce.payment.infrastructure.client.dto.KakaoPayReadyDto;

@ExtendWith(MockitoExtension.class)
class KakaoPayClientTest {

	@Mock
	private RestTemplate restTemplate;

	private KakaoPayGateway kakaoPayGateway;

	@BeforeEach
	void setUp() {
		RetryTemplate retryTemplate = RetryTemplate.builder()
			.maxAttempts(3)
			.fixedBackoff(1000)
			.build();

		kakaoPayGateway = new KakaoPayGateway(restTemplate, retryTemplate);
		ReflectionTestUtils.setField(kakaoPayGateway, "cid", "TC0ONETIME");
		ReflectionTestUtils.setField(kakaoPayGateway, "secretKey", "test-secret");
		ReflectionTestUtils.setField(kakaoPayGateway, "gatewayBaseUrl", "http://localhost:19091");
	}

	@Test
	void ready_API_실패_후_재시도_성공() {
		KakaoPayReadyDto readyDto = new KakaoPayReadyDto(
			"dummy-tid",
			"https://example.com/pc",
			"https://example.com/mobile",
			"2024-04-13T14:30:00"
		);

		when(restTemplate.postForEntity(anyString(), any(), eq(KakaoPayReadyDto.class)))
			.thenThrow(new RuntimeException("1차 실패"))
			.thenThrow(new RuntimeException("2차 실패"))
			.thenReturn(new ResponseEntity<>(readyDto, HttpStatus.OK));

		PaymentReadyResult result = kakaoPayGateway.ready(
			UUID.randomUUID(), UUID.randomUUID(), BigDecimal.valueOf(1000), "테스트"
		);

		assertThat(result.tid()).isEqualTo("dummy-tid");
		verify(restTemplate, times(3)).postForEntity(anyString(), any(), eq(KakaoPayReadyDto.class));
	}
}
