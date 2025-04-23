package com.live_commerce.livebroadcast.infrastructure.websocket;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class ViewerWebSocketHandler extends TextWebSocketHandler {

    private final RedisTemplate<String, String> redisTemplate;

    public ViewerWebSocketHandler(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("연결 성공 broadcastId = " + getBroadcastId(session));
        String broadcastId = getBroadcastId(session);
        String sessionId = session.getId();
        redisTemplate.opsForSet().add("LIVE_VIEWERS:" + broadcastId, sessionId);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String broadcastId = getBroadcastId(session);
        String sessionId = session.getId();
        redisTemplate.opsForSet().remove("LIVE_VIEWERS:" + broadcastId, sessionId);
    }

    private String getBroadcastId(WebSocketSession session) {
        String path = session.getUri().getPath(); // /ws/viewers/{broadcastId}
        return path.substring(path.lastIndexOf("/") + 1);
    }
}
