package com.live_commerce.product.product.application.dto;

import com.live_commerce.product.product.domain.model.Product;

import java.util.UUID;

public record ProductSummaryDto (
        UUID productId,
        String name,
        Integer price
) {
    public static ProductSummaryDto fromEntity(Product product) {
        return new ProductSummaryDto(product.getProductId(), product.getName(), product.getPrice());
    }
}
