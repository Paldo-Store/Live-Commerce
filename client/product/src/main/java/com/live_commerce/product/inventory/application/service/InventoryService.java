package com.live_commerce.product.inventory.application.service;

import com.live_commerce.product.inventory.application.dto.InventoryCreateRequestDto;
import com.live_commerce.product.inventory.application.dto.InventoryResponseDto;
import com.live_commerce.product.inventory.application.mapper.InventoryMapper;
import com.live_commerce.product.inventory.domain.exception.InventoryException;
import com.live_commerce.product.inventory.domain.model.Inventory;
import com.live_commerce.product.inventory.domain.repository.InventoryRepository;
import com.live_commerce.product.product.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;

    @Transactional
    public InventoryResponseDto createInventory(InventoryCreateRequestDto requestDto) {
        if (!productRepository.existsByIdAndDeletedStatusFalse(requestDto.productId())) {
            InventoryException.forProductNotFound();
        }

        Inventory inventory = InventoryMapper.createDtoToEntity(requestDto);
        inventoryRepository.save(inventory);
        return InventoryMapper.entityToDto(inventory);
    }

    public InventoryResponseDto getInventory(UUID id) {
        Inventory inventory = inventoryRepository.findByIdAndDeletedStatusFalse(id).orElse(null);

        if (inventory == null) {
            InventoryException.forInventoryNotFound();
        }

        return InventoryMapper.entityToDto(inventory);
    }
}
