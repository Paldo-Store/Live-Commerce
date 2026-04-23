package com.live_commerce.order.kafkaOrder.product;

import java.util.UUID;

public record OrderCreatedEvent(
        UUID orderId,
        UUID productId,
        int quantity
) {
}