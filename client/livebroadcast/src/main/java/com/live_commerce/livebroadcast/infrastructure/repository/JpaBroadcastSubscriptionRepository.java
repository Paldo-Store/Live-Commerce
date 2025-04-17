package com.live_commerce.livebroadcast.infrastructure.repository;

import com.live_commerce.livebroadcast.domain.model.BroadcastSubscription;
import com.live_commerce.livebroadcast.domain.repository.BroadcastSubscriptionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaBroadcastSubscriptionRepository extends JpaRepository<BroadcastSubscription, UUID>, BroadcastSubscriptionRepository {

    boolean existsByUserIdAndBroadcastIdAndDeletedStatusFalse(UUID userId, UUID broadcastId);

    List<BroadcastSubscription> findAllByUserIdAndDeletedStatusFalse(UUID userId);

    List<BroadcastSubscription> findAllByBroadcastIdAndDeletedStatusFalse(UUID broadcastId);

    Optional<BroadcastSubscription> findByUserIdAndBroadcastIdAndDeletedStatusFalse(UUID userId, UUID broadcastId);

    Page<BroadcastSubscription> findAllByBroadcastId(UUID broadcastId, Pageable pageable);

}
