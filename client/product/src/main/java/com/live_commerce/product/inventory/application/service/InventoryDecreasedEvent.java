package com.live_commerce.product.inventory.application.service;

import java.util.UUID;

public record InventoryDecreasedEvent(
        UUID orderId, // 추적용
        UUID productId,
        int decreasedQuantity // 실제 차감 개수
) {
}
