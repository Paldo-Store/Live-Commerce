package com.live_commerce.livebroadcast.infrastructure.config;

import com.live_commerce.livebroadcast.infrastructure.websocket.ViewerWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final ViewerWebSocketHandler viewerWebSocketHandler;

    public WebSocketConfig(ViewerWebSocketHandler handler) {
        this.viewerWebSocketHandler = handler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(viewerWebSocketHandler, "/ws/viewers/{broadcastId}")
                .setAllowedOrigins("*"); // 시연용
    }
}

