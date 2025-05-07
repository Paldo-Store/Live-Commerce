package com.live_commerce.product.product.domain.model;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "p_product_discount", schema = "products")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductDiscount {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    private Integer discountPrice;

    private LocalDateTime startAt;

    private LocalDateTime endAt;

    private UUID appliedBy;

    @Builder
    public ProductDiscount(Product product, Integer discountPrice, LocalDateTime startAt, LocalDateTime endAt, UUID appliedBy){
        this.product = product;
        this.discountPrice = discountPrice;
        this.startAt = startAt;
        this.endAt = endAt;
        this.appliedBy = appliedBy;
    }

    public boolean isActiveNow() {
        LocalDateTime now = LocalDateTime.now();
        return (startAt.isBefore(now) && endAt.isAfter(now));
    }
}
