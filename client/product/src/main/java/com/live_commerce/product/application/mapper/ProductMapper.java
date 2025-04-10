package com.live_commerce.product.application.mapper;

import com.live_commerce.product.application.dto.ProductCreateRequestDto;
import com.live_commerce.product.application.dto.ProductResponseDto;
import com.live_commerce.product.domain.model.Product;

public class ProductMapper {

    public static Product createDtoToEntity(ProductCreateRequestDto requestDto) {
        return Product.builder()
                .name(requestDto.name())
                .description(requestDto.description())
                .price(requestDto.price())
                .category(requestDto.category())
                .build();
    }

    public static ProductResponseDto entityToDto(Product entity) {
        return new ProductResponseDto(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getPrice(),
                entity.getCategory(),
                entity.getProductStatus()
        );
    }
}
