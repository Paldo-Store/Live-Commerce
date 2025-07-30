package com.live_commerce.product.product.application.dto;

import java.util.UUID;

public record ProductPriceResponseDto(
        UUID productId,
        Integer currentPrice,
        boolean discounted
) {
}
