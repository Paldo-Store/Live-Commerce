package com.live_commerce.order.kafkaOrder.product;


import java.util.UUID;

public record OrderRequestedInventoryEvent(
        UUID orderId,
        UUID productId,
        int quantity
) {
}