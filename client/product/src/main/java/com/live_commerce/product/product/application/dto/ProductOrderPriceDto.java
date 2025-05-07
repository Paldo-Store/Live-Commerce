package com.live_commerce.product.product.application.dto;

import java.util.UUID;

public record ProductOrderPriceDto (
        UUID productId,
        Integer currentPrice
) {
}
