package com.live_commerce.livebroadcast.domain.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "p_broadcast_product", schema = "livebroadcast")
public class BroadcastProduct extends BaseEntity{

    @Id
    @UuidGenerator
    private UUID broadcastProductId;

    private UUID liveBroadcastId;

    private UUID productId;

    private BroadcastProduct(UUID liveBroadcastId, UUID productId) {
        this.liveBroadcastId = liveBroadcastId;
        this.productId = productId;
    }

    public static BroadcastProduct create(UUID liveBroadcastId, UUID productId) {
        return new BroadcastProduct(liveBroadcastId, productId);
    }

}
