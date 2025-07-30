package com.live_commerce.product.inventory.infrastructure.redis;

import java.util.UUID;

public class RedisKey {

    public static final String INVENTORY_PREFIX = "inventory:";

    public static String availableQuantity(UUID productId) {
        return INVENTORY_PREFIX + productId + ":available";
    }
}
