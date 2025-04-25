package com.live_commerce.order.infrastructure.client.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record InventoryDecreaseRequestDto (
        @NotNull UUID productId,
        @Min(1) int quantity
) {
}
