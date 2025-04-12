package com.live_commerce.product.inventory.domain.repository;

import com.live_commerce.product.inventory.domain.model.Inventory;

import java.util.Optional;
import java.util.UUID;

public interface InventoryRepository {

    <S extends Inventory> S save(S inventory);
    Optional<Inventory> findByIdAndDeletedStatusFalse(UUID id);
}
