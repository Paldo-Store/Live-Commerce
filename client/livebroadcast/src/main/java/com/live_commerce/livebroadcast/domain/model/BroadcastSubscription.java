package com.live_commerce.livebroadcast.domain.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "broadcast_subscriptions", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "broadcast_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BroadcastSubscription extends BaseEntity {

    @Id
    @UuidGenerator
    private UUID subscriptionId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "broadcast_id", nullable = false)
    private UUID broadcastId;


    private BroadcastSubscription(UUID userId, UUID broadcastId) {
        this.userId = userId;
        this.broadcastId = broadcastId;
    }

    public static BroadcastSubscription create(UUID userId, UUID broadcastId) {
        return new BroadcastSubscription(userId, broadcastId);
    }
}

