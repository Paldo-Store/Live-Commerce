package com.live_commerce.product.inventory.domain.repository;

import com.live_commerce.product.inventory.domain.model.Inventory;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface InventoryRepository {

    <S extends Inventory> S save(S inventory);
    Optional<Inventory> findByInventoryIdAndDeletedStatusFalse(UUID id);
    Optional<Inventory> findByProductIdAndDeletedStatusFalse(UUID productId);
    boolean existsOrderableInventory(UUID productId, int quantity);

    int decreaseInventoryAtomically(UUID productId, int quantity);
    int increaseInventoryAtomically(UUID productId, int quantity);
}
