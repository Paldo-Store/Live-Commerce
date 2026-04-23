package com.live_commerce.product.inventory.infrastructure.redisson.test;

import com.live_commerce.product.inventory.domain.exception.InventoryException;
import com.live_commerce.product.inventory.domain.model.Inventory;
import com.live_commerce.product.inventory.domain.repository.InventoryRepository;
import com.live_commerce.product.inventory.infrastructure.redisson.DistributedLock;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@RequiredArgsConstructor
public class RedissonLockInventoryTestService {

    private final InventoryRepository inventoryRepository;


    @DistributedLock(key = "#productId")
    @Transactional
    public void decreaseInventory(UUID productId, int quantity) {
        int updated = inventoryRepository.decreaseInventoryAtomically(productId, quantity);
        if (updated == 0) {
            throw InventoryException.forInventoryOutOfStock();
        }
    }

    @Transactional
    public void decreaseInventoryWithoutLock(UUID productId, int quantity) {
        Inventory inventory = inventoryRepository.findByProductIdAndDeletedStatusFalse(productId)
                .orElseThrow(InventoryException::forInventoryNotFound);

        inventory.decrease(quantity);
    }
}
