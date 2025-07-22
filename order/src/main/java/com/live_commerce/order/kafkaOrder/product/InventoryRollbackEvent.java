package com.live_commerce.order.kafkaOrder.product;

import java.util.UUID;

public record InventoryRollbackEvent (
    UUID orderId,
    UUID productId,
    int quantity
){}
