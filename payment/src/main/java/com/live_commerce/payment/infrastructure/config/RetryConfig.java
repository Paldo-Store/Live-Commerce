package com.live_commerce.payment.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.HttpClientErrorException;

@Configuration
public class RetryConfig {

	@Bean
	public RetryTemplate retryTemplate() {
		return RetryTemplate.builder()
			.maxAttempts(3)
			.exponentialBackoff(1000, 2, 4000) // 1s → 2s → 4s
			.notRetryOn(HttpClientErrorException.class) // 4xx는 재시도 무의미
			.build();
	}
}
