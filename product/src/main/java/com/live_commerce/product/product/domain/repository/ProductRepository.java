package com.live_commerce.product.product.domain.repository;

import com.live_commerce.product.product.domain.model.Product;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository {
    <S extends Product> S save(S product);
    Optional<Product> findByProductIdAndDeletedStatusFalse(UUID id);
    boolean existsByProductIdAndDeletedStatusFalse(UUID id);
    List<Product> findAllByProductIdInAndDeletedStatusFalse(List<UUID> ids);
}
