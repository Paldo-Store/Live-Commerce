package com.live_commerce.product.inventory.application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record InventoryDecreaseRequestDto (
        @NotNull UUID productId,
        @Min(1) int quantity
) {
}
