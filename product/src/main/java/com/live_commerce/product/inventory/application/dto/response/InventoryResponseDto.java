package com.live_commerce.product.inventory.application.dto.response;

import com.live_commerce.product.inventory.domain.model.InventoryStatus;

import java.util.UUID;

public record InventoryResponseDto (
        UUID inventoryId,
        UUID productId,
        Integer quantity,
        Integer reservedQuantity,
        Integer availableQuantity,
        InventoryStatus inventoryStatus
) {
}
