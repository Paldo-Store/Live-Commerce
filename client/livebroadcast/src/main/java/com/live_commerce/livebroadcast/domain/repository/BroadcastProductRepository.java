package com.live_commerce.livebroadcast.domain.repository;

import com.live_commerce.livebroadcast.domain.model.BroadcastProduct;
import com.live_commerce.livebroadcast.domain.model.LiveBroadcast;

import java.util.UUID;

public interface BroadcastProductRepository {
    <S extends BroadcastProduct> S save(S broadcastProduct);

    boolean existsByIdAndDeletedStatusFalse(UUID broadcastId);

    boolean existsByIdAndProductId(UUID broadcastId, UUID productId);
}
