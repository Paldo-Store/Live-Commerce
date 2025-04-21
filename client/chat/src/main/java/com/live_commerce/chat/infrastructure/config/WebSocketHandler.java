package com.live_commerce.chat.infrastructure.config;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketHandler extends TextWebSocketHandler {

    // 소켓 세션을 저장할 Set
    private static final Set<WebSocketSession> sessions = new CopyOnWriteArraySet<>();

    // 소켓 연결 확인
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String broadcastId = (String) session.getAttributes().get("broadcastId");
        if (broadcastId != null) {
            log.info("{} 연결됨, broadcastId: {}", session.getId(), broadcastId);
        } else {
            log.info("{} 연결됨, broadcastId 없음", session.getId());
        }
        sessions.add(session);
        session.sendMessage(new TextMessage("WebSocket 연결 완료"));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        log.info("{} 연결 종료", session.getId());
    }

    public void broadcast(UUID broadcastId, String message) {
        for (WebSocketSession session : sessions) {
            if (session.isOpen() && session.getAttributes().containsKey("broadcastId") &&
                    session.getAttributes().get("broadcastId").equals(broadcastId)) {
                try {
                    session.sendMessage(new TextMessage(message));
                } catch (Exception e) {
                    log.error("메시지 전송 오류", e);
                }
            }
        }
    }
}
