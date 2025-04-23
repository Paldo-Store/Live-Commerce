package com.live_commerce.order.infrastructure.client;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record InventoryIncreaseRequestDto (
        @NotNull UUID productId,
        @Min(1) int quantity
) { }
