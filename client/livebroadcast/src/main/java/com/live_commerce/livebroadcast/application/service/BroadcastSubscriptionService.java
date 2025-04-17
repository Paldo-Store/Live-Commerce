package com.live_commerce.livebroadcast.application.service;

import com.live_commerce.livebroadcast.infrastructure.client.notification.BroadcastAlarmRegisterRequest;
import com.live_commerce.livebroadcast.application.dto.response.SubscriptionResponseDto;
import com.live_commerce.livebroadcast.application.mapper.SubscriptionMapper;
import com.live_commerce.livebroadcast.application.validation.LiveBroadcastValidator;
import com.live_commerce.livebroadcast.application.validation.SubscriptionValidator;
import com.live_commerce.livebroadcast.domain.model.BroadcastSubscription;
import com.live_commerce.livebroadcast.domain.model.LiveBroadcast;
import com.live_commerce.livebroadcast.domain.repository.BroadcastSubscriptionRepository;
import com.live_commerce.livebroadcast.infrastructure.client.notification.NotificationClient;
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
    private final NotificationClient notificationClient;

    @Transactional
    public SubscriptionResponseDto subscribe(UUID userId, UUID broadcastId) {
        subscriptionValidator.validateNotSubscribed(userId, broadcastId); // 응답값 예쁘게 안 나옴

        BroadcastSubscription subscription = BroadcastSubscription.create(userId, broadcastId);
        subscriptionRepository.save(subscription);

        LiveBroadcast broadcast = liveBroadcastValidator.validateExists(broadcastId);

        LocalDateTime notifyAt = broadcast.getStartTime().minusMinutes(30);

        // 알림등록 요청 dto 생성
        BroadcastAlarmRegisterRequest alarmRegisterRequest = new BroadcastAlarmRegisterRequest(
                "LIVE_BROADCAST",
                broadcast.getLiveBroadcastId(),
                notifyAt
        );

        notificationClient.registerBroadcastAlarm(alarmRegisterRequest);

        return SubscriptionMapper.toResponse(subscription);
    }

    @Transactional
    public void unsubscribe(UUID userId, UUID broadcastId) {
        BroadcastSubscription subscription = subscriptionValidator.getSubscriptionOrThrow(userId, broadcastId);

        subscription.delete(userId);
    }

    // 사용자의 구독 목록 조회(페이징만 처리)
    @Transactional(readOnly = true)
    public List<BroadcastSubscription> getSubscriptionsByUserId(UUID userId) {
        return subscriptionRepository.findAllByUserIdAndDeletedStatusFalse(userId);
    }

    // 특정 방송의 유저 id 목록. 알림서비스에서 사용될 용도
    @Transactional(readOnly = true)
    public Page<UUID> getSubscriberUserIds(UUID broadcastId, Pageable pageable) {
        return subscriptionRepository.findAllByBroadcastId(broadcastId, pageable)
                .map(BroadcastSubscription::getUserId);
    }



}
