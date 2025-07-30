package com.live_commerce.product.product.application.service;


import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductDiscountCacheService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String KEY_PREFIX = "discount:";

    public void cacheDiscountPrice(UUID productId, Integer discountPrice, Duration duration){
        String key = KEY_PREFIX + productId.toString();
        redisTemplate.opsForValue().set(key, discountPrice.toString(), duration);
    }

    public Optional<Integer> getDiscountPrice(UUID productId){
        String key = KEY_PREFIX + productId.toString();
        String value = redisTemplate.opsForValue().get(key);
        return Optional.ofNullable(value).map(Integer::valueOf);
    }
}
