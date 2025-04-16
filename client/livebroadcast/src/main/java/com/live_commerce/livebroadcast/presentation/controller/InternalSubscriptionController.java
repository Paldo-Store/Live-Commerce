package com.live_commerce.livebroadcast.presentation.controller;

import com.live_commerce.livebroadcast.application.dto.response.SubscriptionUserListResponseDto;
import com.live_commerce.livebroadcast.application.service.BroadcastSubscriptionService;
import com.live_commerce.livebroadcast.presentation.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/internal/livebroadcasts/subscriptions")
@RequiredArgsConstructor
public class InternalSubscriptionController {

    private final BroadcastSubscriptionService subscriptionService;

    // 4. 특정 방송의 구독자 목록 조회
//    @GetMapping("/broadcast/{broadcastId}")
//    public ResponseEntity<ApiResponse<<SubscriptionUserListResponseDto>> getSubscribers(
//            @PathVariable UUID broadcastId
//    ) {
//        List<UUID> userIds = subscriptionService.getSubscriberUserIds(broadcastId);
//        return ResponseEntity.ok(new SubscriptionUserListResponse(userIds));
//    }
}
