package com.live_commerce.livebroadcast.application.service;

import com.live_commerce.livebroadcast.application.dto.response.PageResponse;
import com.live_commerce.livebroadcast.application.validation.NotificationValidator;
import com.live_commerce.livebroadcast.infrastructure.client.notification.BroadcastAlarmRegisterRequest;
import com.live_commerce.livebroadcast.application.dto.response.SubscriptionResponseDto;
import com.live_commerce.livebroadcast.application.mapper.SubscriptionMapper;
import com.live_commerce.livebroadcast.application.validation.LiveBroadcastValidator;
import com.live_commerce.livebroadcast.application.validation.SubscriptionValidator;
import com.live_commerce.livebroadcast.domain.model.BroadcastSubscription;
import com.live_commerce.livebroadcast.domain.model.LiveBroadcast;
import com.live_commerce.livebroadcast.domain.repository.BroadcastSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BroadcastSubscriptionService {

    private final BroadcastSubscriptionRepository subscriptionRepository;
    private final SubscriptionValidator subscriptionValidator;
    private final LiveBroadcastValidator liveBroadcastValidator;
    private final NotificationValidator notificationValidator;

    @Transactional
    public void registerBroadcastAlarm(UUID broadcastId) {
        LiveBroadcast broadcast = liveBroadcastValidator.validateExists(broadcastId);

        LocalDateTime notifyAt = broadcast.getStartTime().minusMinutes(10);

        BroadcastAlarmRegisterRequest request = new BroadcastAlarmRegisterRequest(
                "LIVE_BROADCAST",
                broadcast.getLiveBroadcastId(),
                notifyAt
        );

        notificationValidator.registerAlarmOrThrow(request);
    }

    @Transactional
    public SubscriptionResponseDto subscribe(UUID userId, UUID broadcastId) {
        subscriptionValidator.validateNotSubscribed(userId, broadcastId);

        BroadcastSubscription subscription = BroadcastSubscription.create(userId, broadcastId);
        subscriptionRepository.save(subscription);

        return SubscriptionMapper.toResponse(subscription);
    }

    @Transactional
    public void unsubscribe(UUID userId, UUID broadcastId) {
        BroadcastSubscription subscription = subscriptionValidator.getSubscriptionOrThrow(userId, broadcastId);
        subscription.delete(userId);
    }

    // 사용자의 구독 목록 조회
    @Transactional(readOnly = true)
    public List<BroadcastSubscription> getSubscriptionsByUserId(UUID userId) {
        return subscriptionRepository.findAllByUserIdAndDeletedStatusFalse(userId);
    }

    // 특정 방송의 유저 id 목록. 알림서비스에서 사용될 용도
    @Transactional(readOnly = true)
    public PageResponse<UUID> getSubscriberUserIds(UUID broadcastId, Pageable pageable) {
        Page<UUID> page = subscriptionRepository.findAllByBroadcastId(broadcastId, pageable)
                .map(BroadcastSubscription::getUserId);
        return PageResponse.from(page);
    }

}
