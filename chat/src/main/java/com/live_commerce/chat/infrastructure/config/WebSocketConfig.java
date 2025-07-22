package com.live_commerce.chat.infrastructure.config;
import com.live_commerce.chat.infrastructure.security.CustomWebSocketHandler;
import com.live_commerce.chat.infrastructure.security.WebSocketHandshakeInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final CustomWebSocketHandler customWebSocketHandler;
    private final WebSocketHandshakeInterceptor webSocketHandshakeInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(customWebSocketHandler, "/room")
                //.addInterceptors(webSocketHandshakeInterceptor)
                .setAllowedOriginPatterns("*"); // 프론트 없이 Postman 등 테스트 가능하도록 설정
    }
}