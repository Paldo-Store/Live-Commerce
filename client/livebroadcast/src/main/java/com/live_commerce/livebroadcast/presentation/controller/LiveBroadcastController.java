package com.live_commerce.livebroadcast.presentation.controller;

import com.live_commerce.livebroadcast.application.dto.request.LiveBroadcastCreateRequestDto;
import com.live_commerce.livebroadcast.application.dto.response.LiveBroadcastPageResponse;
import com.live_commerce.livebroadcast.application.dto.response.LiveBroadcastResponseDto;
import com.live_commerce.livebroadcast.application.dto.request.LiveBroadcastUpdateRequestDto;
import com.live_commerce.livebroadcast.application.service.LiveBroadcastService;
import com.live_commerce.livebroadcast.infrastructure.common.ResponseUtil;
import com.live_commerce.livebroadcast.presentation.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.UUID;

@RefreshScope
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/livebroadcasts")
public class LiveBroadcastController {

    private final LiveBroadcastService liveBroadcastService;

    @PostMapping
    public ResponseEntity<ApiResponse<LiveBroadcastResponseDto>> createBroadcast(
            @RequestBody @Valid LiveBroadcastCreateRequestDto requestDto
    ) {
        LiveBroadcastResponseDto responseDto = liveBroadcastService.createBroadcast(requestDto);
        return ResponseUtil.success(responseDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LiveBroadcastResponseDto>> getBroadcast(
            @PathVariable UUID id
    ) {
        LiveBroadcastResponseDto responseDto = liveBroadcastService.getLiveBroadcast(id);
        return ResponseUtil.success(responseDto);
    }


    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<LiveBroadcastResponseDto>> updateBroadcast(
            @PathVariable UUID id,
            @RequestBody LiveBroadcastUpdateRequestDto requestDto
    ) {
        LiveBroadcastResponseDto responseDto = liveBroadcastService.updateLiveBroadcast(id, requestDto);
        return ResponseUtil.success(responseDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteBroadcast(
            @PathVariable UUID id
    ) {
        liveBroadcastService.deleteBroadcast(id);
        return ResponseUtil.success("라이브 방송이 삭제되었습니다.");
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<LiveBroadcastPageResponse>> searchBroadcast(
            @RequestParam(required = false) String keyword,
            Pageable pageable
    ) {
        LiveBroadcastPageResponse response = liveBroadcastService.searchLiveBroadcast(keyword, pageable);
        return ResponseUtil.success(response);
    }
}
