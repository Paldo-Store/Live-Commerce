package com.live_commerce.product.inventory.application.service;

import java.util.UUID;

public record InventoryFailedEvent (
        UUID orderId,
        UUID productId,
        int quantity,
        String message
){
}
