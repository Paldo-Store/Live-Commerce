package com.live_commerce.product.inventory.domain.model;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "p_inventory", schema = "inventories")
public class Inventory extends BaseEntity{

    @Id
    @UuidGenerator
    private UUID id;

    private UUID productId;

    private Integer quantity;

    private Integer reservedQuantity;

    private Integer availableQuantity;

    @Enumerated(EnumType.STRING)
    private InventoryStatus inventoryStatus;

    @Builder
    private Inventory(UUID productId, Integer quantity, Integer reservedQuantity, Integer availableQuantity, InventoryStatus inventoryStatus) {
        this.productId = productId;
        this.quantity = quantity;
        this.reservedQuantity = reservedQuantity;
        this.availableQuantity = availableQuantity;
        this.inventoryStatus = inventoryStatus;
    }

    public static Inventory create(UUID productId, Integer quantity, Integer reservedQuantity, Integer availableQuantity, InventoryStatus inventoryStatus) {
        return Inventory.builder()
                .productId(productId)
                .quantity(quantity)
                .reservedQuantity(reservedQuantity)
                .availableQuantity(availableQuantity)
                .inventoryStatus(inventoryStatus)
                .build();
    }

    public void decrease(int quantity) {
        if (this.quantity < quantity) {
            //TODO 예외처리 - 재고부족
        }
        this.quantity -= quantity;
    }

    public void increase(int quantity) {
        this.quantity += quantity;
    }

}
