package com.live_commerce.livebroadcast.presentation.controller;


import com.live_commerce.livebroadcast.application.dto.response.SubscriptionResponseDto;
import com.live_commerce.livebroadcast.application.mapper.SubscriptionMapper;
import com.live_commerce.livebroadcast.application.service.BroadcastSubscriptionService;
import com.live_commerce.livebroadcast.domain.model.BroadcastSubscription;
import com.live_commerce.livebroadcast.presentation.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
public class BroadcastSubscriptionController {

    private final BroadcastSubscriptionService subscriptionService;


    @PostMapping
    public ResponseEntity<ApiResponse<SubscriptionResponseDto>> subscribe(
            @AuthenticationPrincipal RequestUserDetails userDetails,
            @RequestBody CreateSubscriptionRequest request
    ) {
        BroadcastSubscription subscription = subscriptionService.subscribe(userDetails.userId(), request.broadcastId());
        return ResponseEntity.ok(SubscriptionMapper.toResponse(subscription));
    }

    // 2. 구독 취소
    @DeleteMapping("/{broadcastId}")
    public ResponseEntity<Void> unsubscribe(
            @AuthenticationPrincipal RequestUserDetails userDetails,
            @PathVariable UUID broadcastId
    ) {
        subscriptionService.unsubscribe(userDetails.userId(), broadcastId);
        return ResponseEntity.noContent().build();
    }

    // 3. 내 구독 목록 조회
    @GetMapping
    public ResponseEntity<List<SubscriptionResponse>> getMySubscriptions(
            @AuthenticationPrincipal RequestUserDetails userDetails
    ) {
        List<BroadcastSubscription> subscriptions = subscriptionService.getSubscriptionsByUserId(userDetails.userId());
        List<SubscriptionResponse> responseList = subscriptions.stream()
                .map(SubscriptionMapper::toResponse)
                .toList();
        return ResponseEntity.ok(responseList);
    }
}
