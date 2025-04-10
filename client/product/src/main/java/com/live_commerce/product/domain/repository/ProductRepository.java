package com.live_commerce.product.domain.repository;

import com.live_commerce.product.domain.model.Product;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepository {
    <S extends Product> S save(S liveBroadcast);
    Optional<Product> findByIdAndDeletedStatusFalse(UUID id);
}
