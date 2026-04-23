package com.live_commerce.product.product.infrastructure.repository;

import com.live_commerce.product.product.domain.model.Product;
import com.live_commerce.product.product.domain.repository.ProductRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaProductRepository extends JpaRepository<Product, Long>, ProductRepository {

    // 단건 조회용
    Optional<Product> findByProductIdAndDeletedStatusFalse(UUID id);

    boolean existsByProductIdAndDeletedStatusFalse(UUID id);

    List<Product> findAllByProductIdInAndDeletedStatusFalse(List<UUID> ids);
}
