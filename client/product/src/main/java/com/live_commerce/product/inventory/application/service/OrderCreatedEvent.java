package com.live_commerce.product.inventory.application.service;

import java.util.UUID;

public record OrderCreatedEvent(
        UUID orderId,
        UUID productId,
        int quantity
) {
}
