package com.live_commerce.product.inventory.domain.model;

public enum InventoryStatus {
    AVAILABLE("재고 있음"),
    OUT_OF_STOCK("재고 없음"),
    RESERVED("예약됨"),
    DISCONTINUED("입고 중지됨");

    private final String description;

    InventoryStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
