package com.live_commerce.product.product.infrastructure.kafka.event;

import java.util.UUID;

public record InventorySoldOutEvent (
        UUID productId
){
}
