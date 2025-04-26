package com.live_commerce.product.inventory.application.service;

import java.util.UUID;

public record InventorySoldOutEvent (
        UUID productId
){
}
