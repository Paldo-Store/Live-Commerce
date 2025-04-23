package com.live_commerce.order.infrastructure.client;

import java.util.UUID;

public record ProductCreateResponseDto (
        UUID productId,
        String name,
        String description,
        Integer price,
        ProductCategory category,
        ProductStatus status,
        UUID companyId
) {
}