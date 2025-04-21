package com.live_commerce.chat.infrastructure.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer   {

    private final WebSocketHandler webSocketHandler;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic"); // 구독 채널
        registry.setApplicationDestinationPrefixes("/app"); // 메시지 송신 채널
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins("*") // CORS 설정: 모든 출처에서 연결을 허용
                .setAllowedOrigins("http://localhost:19040")
                .withSockJS(); // 클라이언트의 WebSocket 연결 엔드포인트
    }
}