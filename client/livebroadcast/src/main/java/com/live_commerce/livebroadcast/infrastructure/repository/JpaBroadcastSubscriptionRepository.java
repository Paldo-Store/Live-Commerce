package com.live_commerce.livebroadcast.infrastructure.repository;

import com.live_commerce.livebroadcast.domain.model.BroadcastSubscription;
import com.live_commerce.livebroadcast.domain.repository.BroadcastSubscriptionRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JpaBroadcastSubscriptionRepository extends JpaRepository<BroadcastSubscription, UUID>, BroadcastSubscriptionRepository {

    boolean existsByUserIdAndBroadcastId(UUID userId, UUID broadcastId);

    void deleteByUserIdAndBroadcastId(UUID userId, UUID broadcastId);

    List<BroadcastSubscription> findAllByUserId(UUID userId);

    List<BroadcastSubscription> findAllByBroadcastId(UUID broadcastId);
}
