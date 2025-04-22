package com.live_commerce.chat.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.live_commerce.chat.application.dto.request.ChatCreateRequest;
import com.live_commerce.chat.application.dto.request.ChatMessage;
import com.live_commerce.chat.application.dto.response.ChatCreateResponse;
import com.live_commerce.chat.application.service.ChatService;
import com.live_commerce.chat.infrastructure.client.LiveBroadcastClient;
import com.live_commerce.chat.infrastructure.security.RequestUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RequiredArgsConstructor
@Controller
public class ChatSocketController {

    private final ChatService chatService;
    private final LiveBroadcastClient liveBroadcastClient;

    // 방송 ID별 세션을 저장할 Map
    private final Map<String, Set<WebSocketSession>> broadcastSessions = new ConcurrentHashMap<>();

    // WebSocket 연결 시 호출
    @EventListener
    public void onWebSocketConnected(WebSocketSession session) {
        String broadcastId = getBroadcastId(session);
        broadcastSessions.computeIfAbsent(broadcastId, k -> ConcurrentHashMap.newKeySet()).add(session);
        log.info("WebSocket 연결됨: broadcastId = {}, sessionId = {}", broadcastId, session.getId());
    }

    // WebSocket 연결 종료 시 호출
    @EventListener
    public void onWebSocketDisconnected(WebSocketSession session) {
        broadcastSessions.forEach((broadcastId, sessions) -> sessions.remove(session));
        log.info("WebSocket 연결 종료: sessionId = {}", session.getId());
    }

    // 메시지 수신 후 처리
    public void handleChatMessage(WebSocketSession session, String payload) throws Exception {
        log.info("수신된 메시지: {}", payload);

        // 메시지 파싱
        ChatMessage message = new ObjectMapper().readValue(payload, ChatMessage.class);

        // 사용자 정보 가져오기
        UUID userId = (UUID) session.getAttributes().get("userId");
        String username = (String) session.getAttributes().get("username");
        String role = (String) session.getAttributes().get("role");

        // 방송 ID 유효성 체크
        // TODO: LiveBroadcast에 getBroadcast 로직 구현
        // BroadcastResponse broadcastResponse = liveBroadcastClient.getBroadcast(message.liveBroadcastId());
        // if (!broadcastResponse.broadcastStatus().equals("LIVE")) {
        //     session.sendMessage(new TextMessage("방송이 종료되었거나 유효하지 않습니다."));
        //     return;
        // }

        // 채팅 저장
        ChatCreateRequest chatCreateRequest = new ChatCreateRequest(
                message.userId(),
                message.chatting(),
                message.liveBroadcastId(),
                message.type()
        );

        chatService.createChat(chatCreateRequest, userId);

        // 방송 ID에 맞는 세션에 메시지 브로드캐스트
        UUID broadcastId = message.liveBroadcastId();
        String broadcastMessage = new ObjectMapper().writeValueAsString(message);

        if (broadcastSessions.containsKey(broadcastId)) {
            for (WebSocketSession s : broadcastSessions.get(broadcastId)) {
                if (s.isOpen()) {
                    s.sendMessage(new TextMessage(broadcastMessage));
                }
            }
        }
    }

    // WebSocket에서 방송 ID 추출
    private String getBroadcastId(WebSocketSession session) {
        String uri = String.valueOf(session.getUri());
        if (uri.contains("broadcastId=")) {
            return uri.substring(uri.indexOf("broadcastId=") + 12);
        }
        return "default";
    }
}