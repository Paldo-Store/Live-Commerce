package com.live_commerce.product.inventory.infrastructure.kafka.event;

import java.util.UUID;

public record InventorySoldOutEvent (
        UUID productId
){
}
