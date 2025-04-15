package com.live_commerce.product.inventory.infrastructure.repository;


import com.live_commerce.product.inventory.domain.model.Inventory;
import com.live_commerce.product.inventory.domain.repository.InventoryRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JpaInventoryRepository extends JpaRepository<Inventory, UUID>, InventoryRepository {

    Optional<Inventory> findByInventoryIdAndDeletedStatusFalse(UUID inventoryId);

    Optional<Inventory> findByProductIdAndDeletedStatusFalse(UUID productId);
}
