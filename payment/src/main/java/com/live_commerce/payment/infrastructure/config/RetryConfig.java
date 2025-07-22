package com.live_commerce.payment.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.support.RetryTemplate;

@Configuration
public class RetryConfig {

	@Bean
	public RetryTemplate retryTemplate() {
		return RetryTemplate.builder()
			.maxAttempts(3)        // 최대 3번 시도
			.fixedBackoff(1000)    // 1초 간격 (1000ms)
			.build();
	}
}
