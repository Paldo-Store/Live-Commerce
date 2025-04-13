package com.live_commerce.chat.presentation.controller;

import com.live_commerce.chat.application.dto.request.ChatCreateRequest;
import com.live_commerce.chat.application.dto.response.ChatCreateResponse;
import com.live_commerce.chat.application.dto.response.ChatDeleteResponse;
import com.live_commerce.chat.application.dto.response.ChatGetResponse;
import com.live_commerce.chat.application.service.ChatService;
import com.live_commerce.chat.infrastructure.common.ResponseUtil;
import com.live_commerce.chat.presentation.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chats")
public class ChatController {

    private final ChatService chatService;

    //chat 생성 API
    @PostMapping("/")
    public ResponseEntity<ApiResponse<ChatCreateResponse>> createChat(
            @RequestBody ChatCreateRequest request){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        //userId 가져오기
        String userId = authentication.getName();
        ChatCreateResponse response = chatService.createChat(request, UUID.fromString(userId));
        return ResponseUtil.success(response);
    }

    //chat 전체 조회 API
    //TODO 관리자와 쇼호스트만 가능
    @GetMapping("/")
    public ResponseEntity<ApiResponse<ChatGetResponse>> getAllChats(
            @RequestParam final int page,
            @RequestParam final int size,
            @RequestParam(required = false) final String sort){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElseThrow(() -> new AccessDeniedException("권한이 없습니다."));

        ChatGetResponse response = chatService.getAllChats(page, size, sort, role);
        return ResponseUtil.success(response);
    }

    //한 사용자에 대한 전체 채팅 조회 API
    @GetMapping("/oneUser/")
    public ResponseEntity<ApiResponse<ChatGetResponse>> getChatsByUserId(
            @RequestParam final int page,
            @RequestParam final int size,
            @RequestParam(required = false) final String sort) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName(); // 로그인한 사용자 ID
        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElseThrow(() -> new AccessDeniedException("권한이 없습니다."));

        ChatGetResponse response = chatService.getChatsByUserId(userId, page, size, sort, role);
        return ResponseUtil.success(response);
    }

    //chat 채팅 삭제
    @DeleteMapping("/{chatId}")
    //TODO 관리자들과 본인이 작성한 채팅만 삭제 가능
    public ResponseEntity<ApiResponse<ChatDeleteResponse>> deleteChat(@PathVariable UUID chatId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElseThrow(() -> new AccessDeniedException("권한이 없습니다."));

        ChatDeleteResponse resposne = chatService.deleteChat(chatId, userId, role);
        return ResponseUtil.success(resposne);
    }
}
