package com.live_commerce.product.product.application.dto;

import com.live_commerce.product.product.domain.model.ProductCategory;

import java.util.UUID;

public record ProductCreateRequestDto(
        UUID companyId,
        String name,
        String description,
        Integer price,
        ProductCategory category
){}
