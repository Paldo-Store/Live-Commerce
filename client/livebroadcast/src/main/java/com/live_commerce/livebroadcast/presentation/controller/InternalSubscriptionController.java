package com.live_commerce.livebroadcast.presentation.controller;

import com.live_commerce.livebroadcast.application.dto.response.PageResponse;
import com.live_commerce.livebroadcast.application.dto.response.SubscriptionUserListResponseDto;
import com.live_commerce.livebroadcast.application.service.BroadcastSubscriptionService;
import com.live_commerce.livebroadcast.infrastructure.common.ResponseUtil;
import com.live_commerce.livebroadcast.presentation.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/internal/livebroadcasts/subscriptions")
@RequiredArgsConstructor
public class InternalSubscriptionController {

    private final BroadcastSubscriptionService subscriptionService;

    // 특정 방송의 구독자 목록 조회 - 페이징
    @GetMapping("/broadcast/{broadcastId}/user-ids")
    public ResponseEntity<ApiResponse<PageResponse<UUID>>> getSubscriberUserIds(
            @PathVariable UUID broadcastId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "1000") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UUID> resultPage = subscriptionService.getSubscriberUserIds(broadcastId, pageable);
        return ResponseUtil.success(PageResponse.from(resultPage));
    }

}
