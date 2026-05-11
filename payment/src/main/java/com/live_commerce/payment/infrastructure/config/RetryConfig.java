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
			.fixedBackoff(1000)
			.notRetryOn(HttpClientErrorException.class) // 4xx는 재시도 무의미
			.build();
	}
}
