package com.live_commerce.product.product.application.dto;

import com.live_commerce.product.product.domain.model.ProductCategory;
import com.live_commerce.product.product.domain.model.ProductStatus;

import java.util.UUID;

public record ProductResponseDto (
        UUID id,
        String name,
        String description,
        Integer price,
        ProductCategory category,
        ProductStatus status,
        UUID companyId
) {
}
