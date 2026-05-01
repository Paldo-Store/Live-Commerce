package com.live_commerce.product.inventory.infrastructure.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InventoryRedisService {

    private final RedisTemplate<String, String>  redisTemplate;

    public Long decreaseStock(UUID productId, int quantity) {
        String key = RedisKey.availableQuantity(productId);
        DefaultRedisScript<Long> script = new DefaultRedisScript<>(LuaScripts.STOCK_DECREASE_SCRIPT, Long.class);
        return redisTemplate.execute(script, Collections.singletonList(key), String.valueOf(quantity));
    }

    public void increaseStock(UUID productId, int quantity) {
        redisTemplate.opsForValue().increment(RedisKey.availableQuantity(productId), quantity);
    }

    public void setAvailableQuantity(UUID productId, int quantity) {
        redisTemplate.opsForValue().set(RedisKey.availableQuantity(productId), String.valueOf(quantity));
    }

    public Optional<Integer> getAvailableQuantity(UUID productId) {
        String value = redisTemplate.opsForValue().get(RedisKey.availableQuantity(productId));
        return Optional.ofNullable(value).map(Integer::parseInt);
    }

    public void deleteAvailableQuantity(UUID productId) {
        redisTemplate.delete(RedisKey.availableQuantity(productId));
    }
}
