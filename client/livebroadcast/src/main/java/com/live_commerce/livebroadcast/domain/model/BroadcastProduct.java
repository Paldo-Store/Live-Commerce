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
    private UUID id;

    private UUID broadcastId;

    private UUID productId;

    private BroadcastProduct(UUID broadcastId, UUID productId) {
        this.broadcastId = broadcastId;
        this.productId = productId;
    }

    public static BroadcastProduct create(UUID broadcastId, UUID productId) {
        return new BroadcastProduct(broadcastId, productId);
    }

}
