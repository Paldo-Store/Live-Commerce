package com.live_commerce.livebroadcast.domain.repository;

import com.live_commerce.livebroadcast.domain.model.BroadcastSubscription;

import java.util.List;
import java.util.UUID;

public interface BroadcastSubscriptionRepository {

    <S extends BroadcastSubscription> S save(S broadcastSubscription);

    boolean existsByUserIdAndBroadcastId(UUID userId, UUID broadcastId);

    void deleteByUserIdAndBroadcastId(UUID userId, UUID broadcastId);

    List<BroadcastSubscription> findAllByUserId(UUID userId);

    List<BroadcastSubscription> findAllByBroadcastId(UUID broadcastId);
}
