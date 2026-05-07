package com.live_commerce.product.inventory.infrastructure.repository;

import com.live_commerce.product.inventory.domain.model.InventoryOutbox;
import com.live_commerce.product.inventory.domain.model.InventoryOutboxStatus;
import com.live_commerce.product.inventory.domain.repository.InventoryOutboxRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface JpaInventoryOutboxRepository extends JpaRepository<InventoryOutbox, UUID>, InventoryOutboxRepository {

    List<InventoryOutbox> findTop50ByStatusOrderByCreatedAt(InventoryOutboxStatus status);

    @Modifying
    @Query("UPDATE InventoryOutbox o SET o.status = :status, o.publishedAt = :publishedAt, o.retryCount = :retryCount WHERE o.id = :id")
    void updateRecord(
            @Param("id") UUID id,
            @Param("status") InventoryOutboxStatus status,
            @Param("publishedAt") LocalDateTime publishedAt,
            @Param("retryCount") int retryCount
    );
}
