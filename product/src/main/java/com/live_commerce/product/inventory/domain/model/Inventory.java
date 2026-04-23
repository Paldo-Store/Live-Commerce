package com.live_commerce.product.inventory.domain.model;


import com.live_commerce.product.inventory.domain.exception.InventoryException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Slf4j
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "p_inventory",
        schema = "inventories",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "product_id")
        }
)
public class Inventory extends BaseEntity{

    @Id
    @UuidGenerator
    private UUID inventoryId;

    @Column(name = "product_id", nullable = false)
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
        log.info("감소 시도: 요청 수량={}, 현재 수량={}", quantity, this.availableQuantity);
        if (this.availableQuantity < quantity) {
            log.warn("재고 부족: 요청 수량={}, 현재 수량={}", quantity, this.availableQuantity);
            throw InventoryException.forInventoryOutOfStock();
        }

        this.availableQuantity -= quantity;
        this.quantity -= quantity;

        log.info("재고 차감 성공: 남은 수량={}", this.availableQuantity);

        if (this.availableQuantity <= 0) {
            this.inventoryStatus = InventoryStatus.OUT_OF_STOCK;
        }
    }

    public void increase(int quantity) {
        this.availableQuantity += quantity;
        this.quantity += quantity;

        if (this.inventoryStatus == InventoryStatus.OUT_OF_STOCK && this.availableQuantity > 0) {
            this.inventoryStatus = InventoryStatus.AVAILABLE;
        }
    }

    public void discontinue() {
        this.inventoryStatus = InventoryStatus.DISCONTINUED;
    }

    public void changeStatus(InventoryStatus inventoryStatus) {
        this.inventoryStatus = inventoryStatus;
    }
}
