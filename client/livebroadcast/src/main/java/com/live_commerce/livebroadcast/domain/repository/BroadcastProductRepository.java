package com.live_commerce.livebroadcast.domain.repository;

import com.live_commerce.livebroadcast.domain.model.BroadcastProduct;
import com.live_commerce.livebroadcast.domain.model.LiveBroadcast;

import java.util.Optional;
import java.util.UUID;

public interface BroadcastProductRepository {
    <S extends BroadcastProduct> S save(S broadcastProduct);

    boolean existsByBroadcastIdAndProductIdAndDeletedStatusFalse(UUID broadcastId, UUID productId);

    Optional<BroadcastProduct> findByBroadcastIdAndProductIdAndDeletedStatusFalse(UUID broadcastId, UUID productId);
}
