package com.live_commerce.chat.infrastructure.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer  {

    private final WebSocketHandler webSocketHandler;

//    @Override
//    public void configureMessageBroker(MessageBrokerRegistry registry) {
//        // 클라이언트에게 메시지를 전송할 대역을 설정
//        registry.enableSimpleBroker("/topic");  // /topic/broadcast/{broadcastId}로 구독하는 클라이언트에게 메시지 전송
//        registry.setApplicationDestinationPrefixes("/app");  // 클라이언트가 보낼 메시지의 prefix 설정
//    }
//
//    @Override
//    public void registerStompEndpoints(StompEndpointRegistry registry) {
//        // WebSocket의 엔드포인트 설정
//        registry.addEndpoint("/ws")
//                .setAllowedOriginPatterns("*")
//                .withSockJS();  // /ws가 클라이언트의 WebSocket 연결 엔드포인트
//    }

    @Bean
    public WebSocketHandler webSocketHandler() {
        return new WebSocketHandler();
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry
                // /ws/conn 경로로 WebSocket 연결을 허용
                .addHandler(webSocketHandler, "/ws/conn")
                // CORS 허용
                .setAllowedOrigins("*");
    }
}