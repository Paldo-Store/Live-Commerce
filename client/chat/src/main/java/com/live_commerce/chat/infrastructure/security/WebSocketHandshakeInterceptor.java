package com.live_commerce.chat.infrastructure.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Slf4j
@Component
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            HttpServletRequest httpServletRequest = servletRequest.getServletRequest();
            String userIdHeader = httpServletRequest.getHeader("X-User-Id");
            String username = httpServletRequest.getHeader("X-User-Username");
            String role = httpServletRequest.getHeader("X-User-Role");

            // 인증 실패 시 false 반환
            if (userIdHeader == null || username == null || role == null) {
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return false;
            }

            // WebSocket 세션에 인증 정보 저장
            attributes.put("userId", userIdHeader);
            attributes.put("username", username);
            attributes.put("role", role);
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
        // 후처리 로직이 필요한 경우 구현 (필요 없다면 빈 구현으로 두어도 됩니다)
        log.info("WebSocket handshake completed.");
    }
}