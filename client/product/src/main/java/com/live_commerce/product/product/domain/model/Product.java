package com.live_commerce.product.product.domain.model;


import com.live_commerce.product.product.application.dto.ProductUpdateRequestDto;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "p_product", schema = "products")
public class Product extends BaseEntity{

    @Id
    @UuidGenerator
    private UUID id;

    private UUID companyId;

    private String name;

    private String description;

    private Integer price;

    private ProductCategory category;

    @Enumerated(EnumType.STRING)
    private ProductStatus productStatus;

    @Builder
    private Product(UUID id, UUID companyId, String name, String description, Integer price, ProductCategory category) {
        this.id = id;
        this.companyId = companyId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.productStatus = ProductStatus.PREPARING;
    }

    public static Product create(UUID companyId, String name, String description, Integer price, ProductCategory category) {
        return Product.builder()
                .companyId(companyId)
                .name(name)
                .description(description)
                .price(price)
                .category(category)
                .build();
    }

    public void update(ProductUpdateRequestDto dto) {
        if (dto.name() != null) { this.name = dto.name(); }
        if (dto.description() != null) { this.description = dto.description(); }
        if (dto.price() != null) { this.price = dto.price(); }
        if (dto.category() != null) { this.category = dto.category(); }
        if (dto.productStatus() != null) { this.productStatus = dto.productStatus(); }
    }

}
