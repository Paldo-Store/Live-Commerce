package com.live_commerce.livebroadcast.infrastructure.repository;

import com.live_commerce.livebroadcast.domain.model.BroadcastProduct;
import com.live_commerce.livebroadcast.domain.repository.BroadcastProductRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface JpaBroadcastProductRepository extends JpaRepository<BroadcastProduct, UUID>, BroadcastProductRepository {
    boolean existsByIdAndDeletedStatusFalse(UUID broadcastId);

    boolean existsByIdAndProductId(UUID broadcastId, UUID productId);
}
