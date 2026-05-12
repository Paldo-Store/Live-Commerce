package com.live_commerce.payment.kakao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import com.live_commerce.payment.infrastructure.client.KakaoPayGateway;
import com.live_commerce.payment.infrastructure.client.dto.KakaoPayReadyDto;

@ExtendWith(MockitoExtension.class)
class KakaoPayClientCallbackUrlTest {

	@Mock
	private RestTemplate restTemplate;

	private KakaoPayGateway kakaoPayGateway;

	@BeforeEach
	void setUp() {
		RetryTemplate retryTemplate = RetryTemplate.builder().maxAttempts(1).build();
		kakaoPayGateway = new KakaoPayGateway(restTemplate, retryTemplate);

		ReflectionTestUtils.setField(kakaoPayGateway, "cid", "TC0ONETIME");
		ReflectionTestUtils.setField(kakaoPayGateway, "secretKey", "test-secret");
		ReflectionTestUtils.setField(kakaoPayGateway, "gatewayBaseUrl", "http://localhost:19091");
	}

	@Test
	void ready_호출_시_콜백_URL이_게이트웨이_baseUrl_기준으로_생성된다() {
		when(restTemplate.postForEntity(anyString(), any(), eq(KakaoPayReadyDto.class)))
			.thenReturn(new ResponseEntity<>(
				new KakaoPayReadyDto("TID", "https://kakao/pc", "https://kakao/mobile", "2024-04-13T14:30:00"),
				HttpStatus.OK
			));

		kakaoPayGateway.ready(UUID.randomUUID(), UUID.randomUUID(), BigDecimal.valueOf(5000), "테스트 상품");

		ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);
		verify(restTemplate).postForEntity(anyString(), captor.capture(), eq(KakaoPayReadyDto.class));

		@SuppressWarnings("unchecked")
		Map<String, Object> body = (Map<String, Object>) captor.getValue().getBody();

		assertThat(body.get("approval_url")).isEqualTo("http://localhost:19091/api/v2/payments/approve");
		assertThat(body.get("cancel_url")).isEqualTo("http://localhost:19091/api/v2/payments/cancel");
		assertThat(body.get("fail_url")).isEqualTo("http://localhost:19091/api/v2/payments/fail");
	}
}
