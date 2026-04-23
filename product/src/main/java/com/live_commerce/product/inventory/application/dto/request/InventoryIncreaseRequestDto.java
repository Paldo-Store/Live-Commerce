package com.live_commerce.product.inventory.application.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record InventoryIncreaseRequestDto (
        @NotNull UUID productId,
        @Min(1) int quantity
) { }
