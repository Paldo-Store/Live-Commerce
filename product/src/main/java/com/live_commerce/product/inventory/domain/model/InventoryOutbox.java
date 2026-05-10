package com.live_commerce.product.inventory.domain.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Entity
@Table(name = "p_inventory_outbox", schema = "inventories")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InventoryOutbox {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(nullable = false)
    private UUID orderId;

    @Column(nullable = false)
    private String eventType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InventoryOutboxStatus status;

    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime publishedAt;

    @Column(nullable = false)
    private int retryCount = 0;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public static InventoryOutbox of(UUID orderId, String eventType, String payload) {
        InventoryOutbox outbox = new InventoryOutbox();
        outbox.orderId = orderId;
        outbox.eventType = eventType;
        outbox.payload = payload;
        outbox.status = InventoryOutboxStatus.PENDING;
        outbox.retryCount = 0;
        return outbox;
    }

    public void markPublished() {
        this.status = InventoryOutboxStatus.PUBLISHED;
        this.publishedAt = LocalDateTime.now();
    }

    public void markFailed() {
        this.status = InventoryOutboxStatus.FAILED;
    }

    public void incrementRetry() {
        this.retryCount++;
    }
}
