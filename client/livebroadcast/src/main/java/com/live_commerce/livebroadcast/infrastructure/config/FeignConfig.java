package com.live_commerce.livebroadcast.infrastructure.config;

import com.live_commerce.livebroadcast.domain.exception.LiveBroadcastException;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Bean
    public ErrorDecoder errorDecoder() {
        return new ErrorDecoder.Default();
    }

}

