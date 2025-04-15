package com.live_commerce.product.inventory.application.mapper;

import com.live_commerce.product.inventory.application.dto.InventoryCheckQuantityResponseDto;
import com.live_commerce.product.inventory.application.dto.InventoryCheckResponseDto;
import com.live_commerce.product.inventory.application.dto.InventoryCreateRequestDto;
import com.live_commerce.product.inventory.application.dto.InventoryResponseDto;
import com.live_commerce.product.inventory.domain.model.Inventory;
import org.springframework.stereotype.Component;

@Component
public class InventoryMapper {

    public static Inventory createDtoToEntity(InventoryCreateRequestDto dto) {
        return Inventory.builder()
                .productId(dto.productId())
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

    public static InventoryCheckQuantityResponseDto toCheckQuantityDto(Inventory inventory) {
        return new InventoryCheckQuantityResponseDto(inventory.getAvailableQuantity());
    }

    public static InventoryCheckResponseDto toCheckResponseDto(boolean orderable) {
        return new InventoryCheckResponseDto(orderable);
    }

}
