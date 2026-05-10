package com.live_commerce.product.inventory.domain.repository;

import com.live_commerce.product.inventory.domain.model.InventoryOutbox;
import com.live_commerce.product.inventory.domain.model.InventoryOutboxStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface InventoryOutboxRepository {
    InventoryOutbox save(InventoryOutbox outbox);
    List<InventoryOutbox> findTop50ByStatusOrderByCreatedAt(InventoryOutboxStatus status);
    void updateRecord(UUID id, InventoryOutboxStatus status, LocalDateTime publishedAt, int retryCount);
}
