package com.live_commerce.product.inventory.application.dto;

import java.util.UUID;

public record InventoryCheckResponseDto (
        boolean orderAvailable
) {
}
