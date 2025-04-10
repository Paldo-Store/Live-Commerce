package com.live_commerce.product.infrastructure.repository;

import com.live_commerce.product.domain.model.Product;
import com.live_commerce.product.domain.repository.ProductRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JpaProductRepository extends JpaRepository<Product, Long>, ProductRepository {

    Optional<Product> findByIdAndDeletedStatusFalse(UUID id);
}
