package com.live_commerce.product.product.infrastructure.repository;

import com.live_commerce.product.product.domain.model.ProductDiscount;
import com.live_commerce.product.product.domain.repository.ProductDiscountRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaProductDiscountRepository extends JpaRepository<ProductDiscount, Long>, ProductDiscountRepository {
}
