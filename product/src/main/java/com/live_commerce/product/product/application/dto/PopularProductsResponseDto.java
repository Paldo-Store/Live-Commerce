package com.live_commerce.product.product.application.dto;

import com.live_commerce.product.product.domain.model.ProductCategory;

import java.util.UUID;

public record PopularProductsResponseDto (
        UUID productId,
        String name,
        Integer price,
        String description,
        ProductCategory category,
        long soldCount
) {
}
