package com.live_commerce.livebroadcast.domain.repository;

import com.live_commerce.livebroadcast.domain.model.BroadcastSubscription;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BroadcastSubscriptionRepository {

    <S extends BroadcastSubscription> S save(S broadcastSubscription);

    boolean existsByUserIdAndBroadcastIdAndDeletedStatusFalse(UUID userId, UUID broadcastId);

    List<BroadcastSubscription> findAllByUserIdAndDeletedStatusFalse(UUID userId);

    List<BroadcastSubscription> findAllByBroadcastIdAndDeletedStatusFalse(UUID broadcastId);

    Optional<BroadcastSubscription> findByUserIdAndBroadcastIdAndDeletedStatusFalse(UUID userId, UUID broadcastId);
}
