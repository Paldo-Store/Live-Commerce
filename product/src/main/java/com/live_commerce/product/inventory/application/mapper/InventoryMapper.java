package com.live_commerce.product.inventory.application.mapper;

import com.live_commerce.product.inventory.application.dto.response.InventoryCheckQuantityResponseDto;
import com.live_commerce.product.inventory.application.dto.response.InventoryCheckOrderableResponseDto;
import com.live_commerce.product.inventory.application.dto.request.InventoryCreateRequestDto;
import com.live_commerce.product.inventory.application.dto.response.InventoryResponseDto;
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

    public static InventoryCheckOrderableResponseDto toCheckResponseDto(boolean orderable) {
        return new InventoryCheckOrderableResponseDto(orderable);
    }

}
