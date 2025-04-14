package com.live_commerce.product.inventory.application.service;

import com.live_commerce.product.inventory.application.dto.InventoryCreateRequestDto;
import com.live_commerce.product.inventory.application.dto.InventoryResponseDto;
import com.live_commerce.product.inventory.application.mapper.InventoryMapper;
import com.live_commerce.product.inventory.application.validation.InventoryValidator;
import com.live_commerce.product.inventory.domain.exception.InventoryException;
import com.live_commerce.product.inventory.domain.model.Inventory;
import com.live_commerce.product.inventory.domain.repository.InventoryRepository;
import com.live_commerce.product.product.domain.repository.ProductRepository;
import com.live_commerce.product.product.infrastructure.redisson.DistributedLock;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final InventoryValidator inventoryValidator;
    private final RedissonClient redissonClient;

    @Transactional
    public InventoryResponseDto createInventory(InventoryCreateRequestDto requestDto) {
        if (!productRepository.existsByProductIdAndDeletedStatusFalse(requestDto.productId())) {
            throw InventoryException.forProductNotFound();
        }
        // TODO 상품 한 개당 재고 정보 한 개만 붙어야함

        Inventory inventory = InventoryMapper.createDtoToEntity(requestDto);
        inventoryRepository.save(inventory);
        return InventoryMapper.entityToDto(inventory);
    }

    @Transactional(readOnly = true)
    public InventoryResponseDto getInventory(UUID id) {
        Inventory inventory = inventoryRepository.findByInventoryIdAndDeletedStatusFalse(id).orElse(null);

        if (inventory == null) {
            throw InventoryException.forInventoryNotFound();
        }

        return InventoryMapper.entityToDto(inventory);
    }

    @DistributedLock(key = "#productId")
    @Transactional
    public void decreaseInventory(UUID productId, int quantity) {
        inventoryValidator.validateExistsAndAvailableOrThrow(productId, quantity);

        int updated = inventoryRepository.decreaseInventoryAtomically(productId, quantity);
        if (updated == 0) {
            throw InventoryException.forInventoryOutOfStock();
        }
    }

    @DistributedLock(key = "#productId")
    @Transactional
    public void increaseInventory(UUID productId, int quantity) {
        inventoryValidator.validateExistsOrThrow(productId);

        int updated = inventoryRepository.increaseInventoryAtomically(productId, quantity);
        if (updated == 0) {
            throw InventoryException.forInventoryNotFound();
        }
    }

    public boolean isSoldOut(UUID productId) {
        Inventory inventory = inventoryRepository.findByProductIdAndDeletedStatusFalse(productId)
                .orElse(null);

        if (inventory == null) return true;

        return inventory.getQuantity() <= 0;
    }




}
