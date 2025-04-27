package com.live_commerce.order.kafkaOrder.inventory.message;

import java.util.UUID;

//요청 메시지
public record InventoryCheckRequestMessage(
        String requestId,
        UUID productId,
        int orderQuantity
) {
}