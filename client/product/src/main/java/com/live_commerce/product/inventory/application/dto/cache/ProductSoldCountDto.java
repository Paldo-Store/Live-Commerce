package com.live_commerce.product.inventory.application.dto.cache;

import java.util.UUID;

public record ProductSoldCountDto (
        UUID productId,
        long soldCount
){
}
