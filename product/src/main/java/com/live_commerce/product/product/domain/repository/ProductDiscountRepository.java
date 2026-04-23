package com.live_commerce.product.product.domain.repository;

import com.live_commerce.product.product.domain.model.ProductDiscount;

public interface ProductDiscountRepository {
    <S extends ProductDiscount> S save(S productDiscount);
}
