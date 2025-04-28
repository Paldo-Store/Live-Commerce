package com.live_commerce.product.inventory.application.dto.event;

import java.util.UUID;

public record InventorySoldOutEvent (
        UUID productId
){
}
