package com.live_commerce.product.inventory.infrastructure.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InventoryRedisService {

    private final RedisTemplate<String, String>  redisTemplate;

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
