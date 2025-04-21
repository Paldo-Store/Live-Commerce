package com.live_commerce.chat.presentation.controller;

import com.live_commerce.chat.application.dto.request.ChatCreateRequest;
import com.live_commerce.chat.application.dto.request.ChatMessage;
import com.live_commerce.chat.application.dto.response.BroadcastResponse;
import com.live_commerce.chat.application.dto.response.ChatCreateResponse;
import com.live_commerce.chat.application.service.ChatService;
import com.live_commerce.chat.domain.model.Chat;
import com.live_commerce.chat.infrastructure.client.LiveBroadcastClient;
import com.live_commerce.chat.infrastructure.security.RequestUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

@Slf4j
@RequiredArgsConstructor
@Controller
public class ChatSocketController {

    private final ChatService chatService;
    private final LiveBroadcastClient liveBroadcastClient;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.send")  // /app/chat.send
    @SendTo("/topic/broadcast/{broadcastId}")  // 방송별로 구독한 클라이언트에게 메시지 전달
    public ChatMessage sendMessage(@Payload ChatMessage message,
                                   @AuthenticationPrincipal RequestUserDetails userDetails) {
        log.info("Received message: {}", message);

        //TODO BROADCAST 에러로 잠시 주석 처리
        //방송 상태 확인
        // 1. broadcastId는 외부에서 Feign으로 조회
//        BroadcastResponse broadcastResponse = liveBroadcastClient.getBroadcast(message.liveBroadcastId());
//
//        // 2. 방송이 유효한지 확인
//        if (!broadcastResponse.broadcastStatus().equals("LIVE")) {
//            throw new IllegalStateException("방송이 종료되었거나 유효하지 않습니다.");
//        }

        // WebSocket에서 채팅 생성
        ChatCreateRequest chatCreateRequest = new ChatCreateRequest(
                message.userId(),
                message.chatting(),
                //broadcastResponse.LiveBroadcastId(),  // 방송 ID (feign으로 BroadCast에서 들고온 id)
                message.liveBroadcastId(),
                message.type()
        );

        // 채팅 저장
        ChatCreateResponse response = chatService.createChat(chatCreateRequest, message.userId(), userDetails);

        // 메시지 전송 (동적 브로드캐스트 ID로)
        messagingTemplate.convertAndSend("/topic/broadcast/" + message.liveBroadcastId(), message);

        // 저장된 채팅 메시지를 WebSocket으로 전송
        return message;
    }
}