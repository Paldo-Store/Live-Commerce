package com.live_commerce.inventory.infrastructure.repository;


import com.live_commerce.inventory.domain.model.Inventory;
import com.live_commerce.inventory.domain.repository.InventoryRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaInventoryRepository extends JpaRepository<Inventory, Long>, InventoryRepository {
}
