package com.live_commerce.livebroadcast.presentation.controller;


import com.live_commerce.livebroadcast.application.dto.request.CreateSubscriptionRequestDto;
import com.live_commerce.livebroadcast.application.dto.response.SubscriptionResponseDto;
import com.live_commerce.livebroadcast.application.mapper.SubscriptionMapper;
import com.live_commerce.livebroadcast.application.service.BroadcastSubscriptionService;
import com.live_commerce.livebroadcast.domain.model.BroadcastSubscription;
import com.live_commerce.livebroadcast.infrastructure.common.ResponseUtil;
import com.live_commerce.livebroadcast.infrastructure.security.RequestUserDetails;
import com.live_commerce.livebroadcast.presentation.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/livebroadcasts/subscriptions")
@RequiredArgsConstructor
public class BroadcastSubscriptionController {

    private final BroadcastSubscriptionService subscriptionService;

    // 구독
    @PostMapping
    public ResponseEntity<ApiResponse<SubscriptionResponseDto>> subscribe(
            @AuthenticationPrincipal RequestUserDetails userDetails,
            @RequestBody CreateSubscriptionRequestDto request
    ) {
        SubscriptionResponseDto response = subscriptionService.subscribe(userDetails.getUserId(), request.broadcastId());
        return ResponseUtil.success(response);
    }

    // 구독 취소
    @DeleteMapping("/{broadcastId}")
    public ResponseEntity<ApiResponse<String>> unsubscribe(
            @AuthenticationPrincipal RequestUserDetails userDetails,
            @PathVariable UUID broadcastId
    ) {
        subscriptionService.unsubscribe(userDetails.getUserId(), broadcastId);
        return ResponseUtil.success("구독이 취소되었습니다.");
    }

    // 내 구독 목록 조회
    @GetMapping
    public ResponseEntity<List<SubscriptionResponseDto>> getMySubscriptions(
            @AuthenticationPrincipal RequestUserDetails userDetails
    ) {
        List<BroadcastSubscription> subscriptions = subscriptionService.getSubscriptionsByUserId(userDetails.getUserId());
        List<SubscriptionResponseDto> responseList = subscriptions.stream()
                .map(SubscriptionMapper::toResponse)
                .toList();
        return ResponseEntity.ok(responseList);
    }
}
