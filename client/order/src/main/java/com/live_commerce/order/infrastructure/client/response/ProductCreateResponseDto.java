package com.live_commerce.order.infrastructure.client.response;

import com.live_commerce.order.infrastructure.client.feignEnum.ProductCategory;
import com.live_commerce.order.infrastructure.client.feignEnum.ProductStatus;

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