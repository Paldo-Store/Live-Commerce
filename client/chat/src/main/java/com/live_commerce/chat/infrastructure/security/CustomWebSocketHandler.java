package com.live_commerce.chat.infrastructure.security;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.live_commerce.chat.domain.model.Chat;
import com.live_commerce.chat.domain.model.MessageType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class CustomWebSocketHandler extends TextWebSocketHandler {

    // 방송 ID (String)별로 세션을 관리
    private final Map<String, Set<WebSocketSession>> roomSessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("WebSocket 연결됨: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Chat chatMessage = objectMapper.readValue(message.getPayload(), Chat.class);

        String broadcastId = chatMessage.getLiveBroadcastId().toString();

        // 해당 방송 ID의 세션이 없으면 새로 생성
        roomSessions.putIfAbsent(broadcastId, ConcurrentHashMap.newKeySet());
        roomSessions.get(broadcastId).add(session);

        log.info("받은 메시지: {} from {}", chatMessage.getChatting(), chatMessage.getUserId());

        for (WebSocketSession s : roomSessions.get(broadcastId)) {
            if (s.isOpen()) {
                s.sendMessage(new TextMessage(objectMapper.writeValueAsString(chatMessage)));
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info("WebSocket 연결 종료됨: {}", session.getId());

        roomSessions.forEach((broadcastId, sessions) -> {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                roomSessions.remove(broadcastId);
            }
        });
    }

    public void broadcast(UUID broadcastId, UUID userId, String content) {
        // 메시지 객체 생성
        Chat message = Chat.builder()
                .liveBroadcastId(broadcastId)
                .userId(userId)
                .chatting(content)
                .type(MessageType.ENTER) // 또는 다른 MessageType을 사용할 수 있음
                .build();

        String broadcastIdStr = broadcastId.toString();

        // 해당 broadcastId에 연결된 세션 목록 가져오기
        Set<WebSocketSession> sessions = roomSessions.get(broadcastIdStr);

        if (sessions != null) {
            // 세션이 존재하면, 각 세션에 메시지 전송
            for (WebSocketSession s : sessions) {
                try {
                    if (s.isOpen()) {
                        // 메시지를 JSON으로 변환 후 전송
                        s.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
                    }
                } catch (Exception e) {
                    log.error("브로드캐스트 중 오류", e);
                    // 예외 처리 (필요 시, 실패한 세션 삭제 등)
                }
            }
        }

    }
}