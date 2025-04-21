package com.live_commerce.chat.presentation.controller;

import com.live_commerce.chat.application.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Slf4j
@Controller
@RequiredArgsConstructor
public class WebSocketChatController {

    private final ChatService chatService;

//    @MessageMapping("/chat/{broadcastId}")
//    @SendTo("/topic/chat/{broadcastId}")
//    public ChatMessageResponse handleChat(@DestinationVariable UUID broadcastId, ChatMessageRequest request) {
//        log.info("받은 메시지: {}", request.getMessage());
//        return chatService.saveAndReturnMessage(broadcastId, request);
//    }
}