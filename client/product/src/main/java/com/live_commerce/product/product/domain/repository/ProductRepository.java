package com.live_commerce.product.product.domain.repository;

import com.live_commerce.product.product.domain.model.Product;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository {
    <S extends Product> S save(S product);
    Optional<Product> findByIdAndDeletedStatusFalse(UUID id);
    boolean existsByIdAndDeletedStatusFalse(UUID id);
    List<Product> findAllByIdInAndDeletedStatusFalse(List<UUID> ids);
}
