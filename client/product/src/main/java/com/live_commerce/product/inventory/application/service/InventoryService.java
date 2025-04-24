package com.live_commerce.product.inventory.application.service;

import com.live_commerce.product.inventory.application.dto.request.InventoryCreateRequestDto;
import com.live_commerce.product.inventory.application.dto.response.InventoryCheckQuantityResponseDto;
import com.live_commerce.product.inventory.application.dto.response.InventoryCheckOrderableResponseDto;
import com.live_commerce.product.inventory.application.dto.response.InventoryResponseDto;
import com.live_commerce.product.inventory.application.mapper.InventoryMapper;
import com.live_commerce.product.inventory.application.validation.InventoryValidator;
import com.live_commerce.product.inventory.domain.exception.InventoryException;
import com.live_commerce.product.inventory.domain.model.Inventory;
import com.live_commerce.product.inventory.domain.repository.InventoryRepository;
import com.live_commerce.product.product.domain.repository.ProductRepository;
import com.live_commerce.product.inventory.infrastructure.redisson.DistributedLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final InventoryValidator inventoryValidator;


    @Transactional
    public InventoryResponseDto createInventory(InventoryCreateRequestDto requestDto) {
        if (!productRepository.existsByProductIdAndDeletedStatusFalse(requestDto.productId())) {
            throw InventoryException.forProductNotFound();
        }

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
        int updated = inventoryRepository.decreaseInventoryAtomically(productId, quantity);
        if (updated == 0) {
            throw InventoryException.forInventoryOutOfStock();
        }
    }

    @DistributedLock(key = "#productId")
    @Transactional
    public void increaseInventory(UUID productId, int quantity) {
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


    public InventoryCheckQuantityResponseDto checkInventoryQuantity(UUID productId) {
        Inventory inventory = inventoryValidator.validateAndGetActiveInventory(productId);
        return InventoryMapper.toCheckQuantityDto(inventory);
    }

    public InventoryCheckOrderableResponseDto checkOrderableInventory(UUID productId, int orderQuantity) {
        boolean orderable = inventoryValidator.checkOrderable(productId, orderQuantity);
        return InventoryMapper.toCheckResponseDto(orderable);
    }

    @DistributedLock(key = "#productId", waitTime = 1, leaseTime = 2) // 강제 제한
    public void lockForTesting(UUID productId, int holdSeconds) {
        try {
            Thread.sleep(holdSeconds * 1000L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


}
