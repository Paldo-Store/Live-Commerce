package com.live_commerce.product.inventory.application.validation;

import com.live_commerce.product.inventory.domain.exception.InventoryException;
import com.live_commerce.product.inventory.domain.model.Inventory;
import com.live_commerce.product.inventory.domain.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class InventoryValidator {

    private final InventoryRepository inventoryRepository;

    // "재고 존재 여부"를 검증하고, 재고 엔티티를 리턴함
    public Inventory validateAndGetActiveInventory(UUID productId) {
        return inventoryRepository.findByProductIdAndDeletedStatusFalse(productId)
                .orElseThrow(InventoryException::forInventoryNotFound);
    }

    // 이미 조회된 인벤토리에 대해 수량이 충분한지 검증만 수행
    public void validateAvailableQuantityOrThrow(Inventory inventory, int quantity) {
        if (inventory.getAvailableQuantity() < quantity) {
            throw InventoryException.forInventoryOutOfStock();
        }
    }

    // "존재 + 수량"을 동시에 검증 (서비스에 필요한 통합 메서드)
    public void validateExistsAndAvailableOrThrow(UUID productId, int quantity) {
        Inventory inventory = validateAndGetActiveInventory(productId);
        validateAvailableQuantityOrThrow(inventory, quantity);
    }

    // 존재 여부만 검증 (increase 용도)
    public void validateExistsOrThrow(UUID productId) {
        boolean exists = inventoryRepository.findByProductIdAndDeletedStatusFalse(productId).isPresent();
        if (!exists) {
            throw InventoryException.forInventoryNotFound();
        }
    }
}

