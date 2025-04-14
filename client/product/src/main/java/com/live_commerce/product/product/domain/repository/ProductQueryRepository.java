package com.live_commerce.product.product.domain.repository;

import com.live_commerce.product.product.application.dto.ProductSearchCondition;
import com.live_commerce.product.product.domain.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductQueryRepository{

    Page<Product> search(ProductSearchCondition condition, Pageable pageable);
}
