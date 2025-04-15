package com.live_commerce.product.inventory.application.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record InventoryCheckRequestDto (
        @NotNull(message = "상품 id를 입력해주세요") UUID productId,
        @NotNull(message = "재고 수량을 입력해주세요") int orderQuantity
) {
}
