package com.live_commerce.livebroadcast.application.validation;

import com.live_commerce.livebroadcast.domain.exception.LiveBroadcastException;
import com.live_commerce.livebroadcast.domain.model.BroadcastSubscription;
import com.live_commerce.livebroadcast.domain.repository.BroadcastSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SubscriptionValidator {

    private final BroadcastSubscriptionRepository subscriptionRepository;

    // 사용자가 해당 방송 이미 구독하였는지 확인
    public void validateNotSubscribed(UUID userId, UUID broadcastId) {
        boolean exists = subscriptionRepository.existsByUserIdAndBroadcastIdAndDeletedStatusFalse(userId, broadcastId);
        if (exists) {
            throw LiveBroadcastException.alreadySubscribed();
        }
    }

    // 구독 확인 후 객체 반환
    public BroadcastSubscription getSubscriptionOrThrow(UUID userId, UUID broadcastId) {
        return subscriptionRepository.findByUserIdAndBroadcastIdAndDeletedStatusFalse(userId, broadcastId)
                .orElseThrow(LiveBroadcastException::forSubscriptionNotFound);
    }


}
