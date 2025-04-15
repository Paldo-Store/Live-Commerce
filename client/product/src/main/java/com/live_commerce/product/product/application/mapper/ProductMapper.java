package com.live_commerce.product.product.application.mapper;

import com.live_commerce.product.inventory.application.service.InventoryService;
import com.live_commerce.product.product.application.dto.ProductCreateRequestDto;
import com.live_commerce.product.product.application.dto.ProductCreateResponseDto;
import com.live_commerce.product.product.application.dto.ProductResponseDto;
import com.live_commerce.product.product.domain.model.Product;
import com.live_commerce.product.product.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;


public class ProductMapper {

    public static Product createDtoToEntity(ProductCreateRequestDto requestDto, UUID companyId) {
        return Product.create(
                companyId,
                requestDto.name(),
                requestDto.description(),
                requestDto.price(),
                requestDto.category()
        );
    }

    public static ProductCreateResponseDto entityToCreateDto(Product entity) {
        return new ProductCreateResponseDto(
                entity.getProductId(),
                entity.getName(),
                entity.getDescription(),
                entity.getPrice(),
                entity.getCategory(),
                entity.getProductStatus(),
                entity.getCompanyId()
        );
    }

    public static ProductResponseDto entityToDto(Product entity, boolean soldOut) {
        return new ProductResponseDto(
                entity.getProductId(),
                entity.getName(),
                entity.getDescription(),
                entity.getPrice(),
                entity.getCategory(),
                entity.getProductStatus(),
                entity.getCompanyId(),
                soldOut
        );
    }
}
