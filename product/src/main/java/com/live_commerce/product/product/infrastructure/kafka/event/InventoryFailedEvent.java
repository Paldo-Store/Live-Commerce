package com.live_commerce.product.product.infrastructure.kafka.event;

import java.util.UUID;

public record InventoryFailedEvent (
        UUID orderId,
        UUID productId,
        int quantity,
        String message
){
}
