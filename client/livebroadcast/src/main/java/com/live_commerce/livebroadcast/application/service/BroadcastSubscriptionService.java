package com.live_commerce.livebroadcast.application.service;

import com.live_commerce.livebroadcast.domain.model.BroadcastSubscription;
import com.live_commerce.livebroadcast.domain.repository.BroadcastSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BroadcastSubscriptionService {

    private final BroadcastSubscriptionRepository broadcastSubscriptionRepository;

    public void subscribe(UUID userId, UUID broadcastId) {
        if (broadcastSubscriptionRepository.existsByUserIdAndBroadcastId(userId, broadcastId)) {
            throw new IllegalStateException("이미 구독한 방송입니다.");
        }
        broadcastSubscriptionRepository.save(BroadcastSubscription.create(userId, broadcastId));
    }

    public void unsubscribe(UUID userId, UUID broadcastId) {
        broadcastSubscriptionRepository.deleteByUserIdAndBroadcastId(userId, broadcastId);
    }

    public List<UUID> getSubscriberUserIds(UUID broadcastId) {
        return broadcastSubscriptionRepository.findAllByBroadcastId(broadcastId)
                .stream()
                .map(BroadcastSubscription::getUserId)
                .toList();
    }
}
