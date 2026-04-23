package com.live_commerce.livebroadcast.presentation.controller;

import com.live_commerce.livebroadcast.application.dto.request.LiveBroadcastCreateRequestDto;
import com.live_commerce.livebroadcast.application.dto.response.LiveBroadcastPageResponse;
import com.live_commerce.livebroadcast.application.dto.response.LiveBroadcastResponseDto;
import com.live_commerce.livebroadcast.application.dto.request.LiveBroadcastUpdateRequestDto;
import com.live_commerce.livebroadcast.application.dto.response.PageResponse;
import com.live_commerce.livebroadcast.application.service.BroadcastSubscriptionService;
import com.live_commerce.livebroadcast.application.service.LiveBroadcastService;
import com.live_commerce.livebroadcast.infrastructure.common.ResponseUtil;
import com.live_commerce.livebroadcast.infrastructure.security.RequestUserDetails;
import com.live_commerce.livebroadcast.presentation.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


import java.util.UUID;

@RefreshScope
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/livebroadcasts")
public class LiveBroadcastController {

    private final LiveBroadcastService liveBroadcastService;
    private final BroadcastSubscriptionService broadcastSubscriptionService;

    /**
     * 방송 생성
     */
    @PreAuthorize("hasAnyRole('MASTER', 'SHOW_HOST')")
    @PostMapping
    public ResponseEntity<ApiResponse<LiveBroadcastResponseDto>> createBroadcast(
            @RequestBody @Valid LiveBroadcastCreateRequestDto requestDto
    ) {
        LiveBroadcastResponseDto responseDto = liveBroadcastService.createBroadcast(requestDto);
        return ResponseUtil.success(responseDto);
    }

    /**
     * 방송 조회
     */
    @PreAuthorize("hasAnyRole('MASTER','SHOW_HOST','SELLER','CUSTOMER')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LiveBroadcastResponseDto>> getBroadcast(
            @PathVariable UUID id
    ) {
        LiveBroadcastResponseDto responseDto = liveBroadcastService.getLiveBroadcast(id);
        return ResponseUtil.success(responseDto);
    }

    /**
     * 방송 수정
     * TODO 방송 시작시간 수정 시 알림 서비스 삭제후 재생성 로직 추가해야함
     */
    @PreAuthorize("hasAnyRole('MASTER','SHOW_HOST')")
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<LiveBroadcastResponseDto>> updateBroadcast(
            @PathVariable UUID id,
            @RequestBody LiveBroadcastUpdateRequestDto requestDto,
            @AuthenticationPrincipal RequestUserDetails userDetails
            ) {
        LiveBroadcastResponseDto responseDto = liveBroadcastService.updateLiveBroadcast(id, requestDto, userDetails);
        return ResponseUtil.success(responseDto);
    }

    /**
     * 방송 삭제
     */
    @PreAuthorize("hasAnyRole('MASTER','SHOW_HOST')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteBroadcast(
            @PathVariable UUID id,
            @AuthenticationPrincipal RequestUserDetails userDetails
    ) {
        liveBroadcastService.deleteBroadcast(id, userDetails);
        return ResponseUtil.success("라이브 방송이 삭제되었습니다.");
    }

    /**
     * 방송 검색
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<LiveBroadcastPageResponse>> searchBroadcast(
            @RequestParam(required = false) String keyword,
            Pageable pageable
    ) {
        LiveBroadcastPageResponse response = liveBroadcastService.searchLiveBroadcast(keyword, pageable);
        return ResponseUtil.success(response);
    }


    /**
     * 구독자 목록 조회 (알림서비스에서 사용)
     */
    @PreAuthorize("hasRole('MASTER')")
    @GetMapping("/{broadcastId}/subscribers")
    public PageResponse<UUID> getSubscribers(
            @PathVariable UUID broadcastId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size
    ) {
        if (size != 100) { size = 100; }

        Pageable pageable = PageRequest.of(page, size);
        return broadcastSubscriptionService.getSubscriberUserIds(broadcastId, pageable);
    }






}
