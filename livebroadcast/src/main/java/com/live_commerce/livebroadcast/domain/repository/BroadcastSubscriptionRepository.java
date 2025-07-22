package com.live_commerce.livebroadcast.domain.repository;

import com.live_commerce.livebroadcast.domain.model.BroadcastSubscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BroadcastSubscriptionRepository {

    long count();

    <S extends BroadcastSubscription> S save(S broadcastSubscription);

    boolean existsByUserIdAndBroadcastIdAndDeletedStatusFalse(UUID userId, UUID broadcastId);

    List<BroadcastSubscription> findAllByUserIdAndDeletedStatusFalse(UUID userId);

    List<BroadcastSubscription> findAllByBroadcastIdAndDeletedStatusFalse(UUID broadcastId);

    Optional<BroadcastSubscription> findByUserIdAndBroadcastIdAndDeletedStatusFalse(UUID userId, UUID broadcastId);

    Page<BroadcastSubscription> findAllByBroadcastId(UUID broadcastId, Pageable pageable);
}
