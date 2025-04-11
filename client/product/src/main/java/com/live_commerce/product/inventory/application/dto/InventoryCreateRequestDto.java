package com.live_commerce.product.inventory.application.dto;

import com.live_commerce.product.inventory.domain.model.InventoryStatus;

import java.util.UUID;

public record InventoryCreateRequestDto (
        UUID productId,
        Integer quantity,
        Integer reservedQuantity,
        Integer availableQuantity,
        InventoryStatus inventoryStatus
) {
}
