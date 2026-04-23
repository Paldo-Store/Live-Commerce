package com.live_commerce.chat.infrastructure.security;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.live_commerce.chat.application.dto.request.ChatCreateRequest;
import com.live_commerce.chat.application.exception.ChatException;
import com.live_commerce.chat.application.service.ChatService;
import com.live_commerce.chat.domain.model.Chat;
import com.live_commerce.chat.domain.model.MessageType;
import com.live_commerce.chat.infrastructure.client.BroadcastClient;
import com.live_commerce.chat.infrastructure.client.BroadcastStatus;
import com.live_commerce.chat.infrastructure.client.BroadcastStatusResponse;
import com.live_commerce.chat.presentation.common.ApiResponse;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomWebSocketHandler extends TextWebSocketHandler {

    // 방송 ID (String)별로 접속한 유저들의 세션을 관리
    private final Map<String, Set<WebSocketSession>> roomSessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ChatService chatService;  // chatService 주입
    private final JwtUtil jwtUtil;
    private final BroadcastClient broadcastClient;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
//        List<String> authHeaders = session.getHandshakeHeaders().get("Authorization");
//        log.info("authHeaders: {}", authHeaders);
//
//        String token = authHeaders.get(0).replace("Bearer ", "").trim();
//        log.info("토큰 : " + token);
//
//        // Token이 유효하면 claims에서 사용자 정보 추출
//        Claims claims = jwtUtil.parseClaims(token);
//        UUID userId = UUID.fromString(claims.get("userId", String.class));
//        String role = claims.get("role", String.class);
//        log.info("userId와 role 들고오기!");
//
//        // 세션에 userId, role을 저장
//        session.getAttributes().put("userId", userId);
//        session.getAttributes().put("role", role);
//
//        log.info("WebSocket 연결됨 - userId: {}, role: {}", userId, role);
        log.info("WebSocket 연결됨: {}");
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

        // 메시지 파싱
        ChatCreateRequest request = objectMapper.readValue(message.getPayload(), ChatCreateRequest.class);

        //TODO 사용자 인증 정보 추출
//        UUID userId = (UUID) session.getAttributes().get("userId");
//        String role = (String) session.getAttributes().get("role");
//
//        if (userId == null || role == null) {
//            log.info("userId나 role이 없습니다");
//            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Unauthorized"));
//            return;
//        }
        UUID userId = request.userId();

        //방송중인지 검증
//        ApiResponse<BroadcastStatusResponse> response = broadcastClient.getBroadcast(request.liveBroadcastId());
//        BroadcastStatusResponse statusResponse = response.getData();
//        if (statusResponse == null || statusResponse.broadcastStatus() != BroadcastStatus.LIVE) {
//            throw new ChatException("방송 중일 때만 채팅이 가능합니다.");
//        }
//        log.info("방송 체크 완료");

        // 메시지 타입에 따른 처리
        String broadcastId = request.liveBroadcastId().toString();
        roomSessions.putIfAbsent(broadcastId, ConcurrentHashMap.newKeySet());

        switch (request.messageType()) {
            case ENTER -> handleEnter(session, userId, request);
            case TALK -> handleTalk(session, userId, request);
            case LEAVE -> handleLeave(session, userId, request);
            default -> log.warn("알 수 없는 메시지 타입: {}", request.messageType());
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

    private void handleEnter(WebSocketSession session, UUID userId, ChatCreateRequest request) {
        String broadcastId = request.liveBroadcastId().toString();
        roomSessions.get(broadcastId).add(session);

        // 입장 메시지 생성
        Chat enterMessage = Chat.builder()
                .liveBroadcastId(request.liveBroadcastId())
                .userId(userId)
                .chatting("입장하셨습니다.")
                .type(MessageType.ENTER)
                .build();

        broadcastMessage(broadcastId, enterMessage);
        log.info("입장 처리 완료 - userId: {}, 방송: {}", userId, broadcastId);
    }

    private void handleTalk(WebSocketSession session, UUID userId, ChatCreateRequest request) {
        UUID sender = request.userId();
        String broadcastId = request.liveBroadcastId().toString();

        // 채팅 메시지 생성
        Chat chatMessage = Chat.builder()
                .chatting(request.chatting())
                .userId(userId)
                .liveBroadcastId(request.liveBroadcastId())
                .type(MessageType.TALK)
                .build();

        chatService.createChat(request, sender); // DB 저장
        roomSessions.get(broadcastId).add(session);

        broadcastMessage(broadcastId, chatMessage);
        log.info("TALK 메시지 전송 - userId: {}, 방송: {}, 내용: {}", sender, broadcastId, chatMessage.getChatting());
    }

    private void handleLeave(WebSocketSession session, UUID userId, ChatCreateRequest request) {
        String broadcastId = request.liveBroadcastId().toString();

        // 세션에서 채팅방 퇴장 처리
        Set<WebSocketSession> sessions = roomSessions.getOrDefault(broadcastId, Set.of());
        sessions.remove(session);

        // 퇴장 메시지 생성
        Chat leaveMessage = Chat.builder()
                .liveBroadcastId(request.liveBroadcastId())
                .userId(userId)
                .chatting("퇴장하셨습니다.")
                .type(MessageType.LEAVE)
                .build();

        broadcastMessage(broadcastId, leaveMessage);
        log.info("퇴장 처리 완료 - userId: {}, 방송: {}", userId, broadcastId);
    }

    // 해당 방송 ID의 모든 세션에 메시지를 브로드캐스트하는 메서드
    private void broadcastMessage(String broadcastId, Chat message) {
        Set<WebSocketSession> sessions = roomSessions.get(broadcastId);

        if (sessions != null) {
            // 각 세션에 메시지 전송
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

