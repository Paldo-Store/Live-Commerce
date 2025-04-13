package com.live_commerce.chat.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 클라이언트에게 메시지를 전송할 대역을 설정
        registry.enableSimpleBroker("/topic");  // /topic/broadcast/{broadcastId}로 구독하는 클라이언트에게 메시지 전송
        registry.setApplicationDestinationPrefixes("/app");  // 클라이언트가 보낼 메시지의 prefix 설정
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket의 엔드포인트 설정
        registry.addEndpoint("/ws").withSockJS();  // /ws가 클라이언트의 WebSocket 연결 엔드포인트
    }
}