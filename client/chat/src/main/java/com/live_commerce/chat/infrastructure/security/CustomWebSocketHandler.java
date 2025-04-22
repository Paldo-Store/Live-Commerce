package com.live_commerce.chat.infrastructure.security;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CustomWebSocketHandler extends TextWebSocketHandler {

    private final Map<String, List<WebSocketSession>> broadcastSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String broadcastId = extractBroadcastId(session);
        broadcastSessions.computeIfAbsent(broadcastId, k -> new ArrayList<>()).add(session);
        System.out.println("✔ WebSocket 연결됨 | 방송 ID: " + broadcastId);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String broadcastId = extractBroadcastId(session);
        List<WebSocketSession> sessions = broadcastSessions.getOrDefault(broadcastId, new ArrayList<>());
        sessions.remove(session);
        System.out.println("✖ WebSocket 연결 종료 | 방송 ID: " + broadcastId);
    }

    public void broadcast(UUID broadcastId, String message) {
        List<WebSocketSession> sessions = broadcastSessions.getOrDefault(broadcastId, new ArrayList<>());
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(message));
                    System.out.println("📢 메시지 전송: " + message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String extractBroadcastId(WebSocketSession session) {
        String uri = session.getUri().toString();
        return uri.substring(uri.lastIndexOf("/") + 1);
    }
}