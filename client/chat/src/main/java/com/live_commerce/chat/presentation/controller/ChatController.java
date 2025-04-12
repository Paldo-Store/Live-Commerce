package com.live_commerce.chat.presentation.controller;

import com.live_commerce.chat.application.dto.request.ChatCreateRequest;
import com.live_commerce.chat.application.dto.response.ChatCreateResponse;
import com.live_commerce.chat.application.service.ChatService;
import com.live_commerce.chat.infrastructure.common.ResponseUtil;
import com.live_commerce.chat.presentation.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chats")
public class ChatController {

    private final ChatService chatService;

    @RequestMapping("/")
    public ResponseEntity<ApiResponse<ChatCreateResponse>> createChat(
            @RequestBody ChatCreateRequest request){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        //userId 가져오기
        String userId = authentication.getName();
        ChatCreateResponse response = chatService.createChat(request, userId);
        return ResponseUtil.success(response);
    }
}
