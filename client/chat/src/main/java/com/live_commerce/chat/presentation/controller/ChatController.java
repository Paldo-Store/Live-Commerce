package com.live_commerce.chat.presentation.controller;

import com.live_commerce.chat.application.dto.request.ChatCreateRequest;
import com.live_commerce.chat.application.dto.response.ChatCreateResponse;
import com.live_commerce.chat.application.dto.response.ChatDeleteResponse;
import com.live_commerce.chat.application.dto.response.ChatGetResponse;
import com.live_commerce.chat.application.service.ChatService;
import com.live_commerce.chat.infrastructure.common.ResponseUtil;
import com.live_commerce.chat.infrastructure.security.RequestUserDetails;
import com.live_commerce.chat.presentation.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chattings")
public class ChatController {

    private final ChatService chatService;

    //chat 생성 API
    @PostMapping("/")
    public ResponseEntity<ApiResponse<ChatCreateResponse>> createChat(
            @RequestBody ChatCreateRequest request,
            @AuthenticationPrincipal RequestUserDetails userDetails) {

        //userId 가져오기
        UUID userId = userDetails.getUserId();

        //권한은 userDetails로 넘겨준다.
        ChatCreateResponse response = chatService.createChat(request, userId, userDetails);
        return ResponseUtil.success(response);
    }

    //chat 전체 조회 API
    //TODO 관리자와 쇼호스트만 가능
    @GetMapping("/")
    public ResponseEntity<ApiResponse<ChatGetResponse>> getAllChats(
            @RequestParam final int page,
            @RequestParam final int size,
            @RequestParam(required = false) final String sort,
            @AuthenticationPrincipal RequestUserDetails userDetails){

        log.info("CHAT 전체 조회");

        ChatGetResponse response = chatService.getAllChats(page, size, sort, userDetails);
        return ResponseUtil.success(response);
    }

    //한 사용자에 대한 전체 채팅 조회 API
    @GetMapping("/oneUser/")
    public ResponseEntity<ApiResponse<ChatGetResponse>> getChatsByUserId(
            @RequestParam final int page,
            @RequestParam final int size,
            @RequestParam(required = false) final String sort,
            @AuthenticationPrincipal RequestUserDetails userDetails) {

        //userId 가져오기
        UUID userId = userDetails.getUserId();

        ChatGetResponse response = chatService.getChatsByUserId(userId, page, size, sort, userDetails);
        return ResponseUtil.success(response);
    }

    //chat 채팅 삭제
    @DeleteMapping("/{chatId}")
    //TODO 관리자들과 본인이 작성한 채팅만 삭제 가능
    public ResponseEntity<ApiResponse<ChatDeleteResponse>> deleteChat(
            @PathVariable UUID chatId,
            @AuthenticationPrincipal RequestUserDetails userDetails) {

        //userId 가져오기
        UUID userId = userDetails.getUserId();
        String role = userDetails.getAuthorities().iterator().next().getAuthority();

        ChatDeleteResponse resposne = chatService.deleteChat(chatId, userId, userDetails);
        return ResponseUtil.success(resposne);
    }


}
