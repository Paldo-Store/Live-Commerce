package com.live_commerce.order.infrastructure.config;

import feign.Client;
import feign.httpclient.ApacheHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Bean
    public Client feignClient() {
        return new ApacheHttpClient(); // PATCH 지원되는 HTTP 클라이언트
    }
}