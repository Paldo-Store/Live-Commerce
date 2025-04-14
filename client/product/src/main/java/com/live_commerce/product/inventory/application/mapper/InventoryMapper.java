package com.live_commerce.product.inventory.application.mapper;

import com.live_commerce.product.inventory.application.dto.InventoryCreateRequestDto;
import com.live_commerce.product.inventory.application.dto.InventoryResponseDto;
import com.live_commerce.product.inventory.domain.model.Inventory;

public class InventoryMapper {

    public static Inventory createDtoToEntity(InventoryCreateRequestDto dto) {
        return Inventory.builder()
                .quantity(dto.quantity())
                .reservedQuantity(dto.reservedQuantity())
                .availableQuantity(dto.availableQuantity())
                .inventoryStatus(dto.inventoryStatus())
                .build();
    }

    public static InventoryResponseDto entityToDto(Inventory entity) {
        return new InventoryResponseDto(
                entity.getInventoryId(),
                entity.getProductId(),
                entity.getQuantity(),
                entity.getReservedQuantity(),
                entity.getAvailableQuantity(),
                entity.getInventoryStatus()
        );
    }
}
