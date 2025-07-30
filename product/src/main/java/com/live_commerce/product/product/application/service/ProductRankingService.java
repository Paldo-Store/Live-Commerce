package com.live_commerce.product.product.application.service;

import com.live_commerce.product.inventory.application.dto.cache.ProductSoldCountDto;
import com.live_commerce.product.product.application.dto.PopularProductsResponseDto;
import com.live_commerce.product.product.domain.model.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ProductRankingService {

    private final StringRedisTemplate redisTemplate;
    private final ProductService productService;

    public List<PopularProductsResponseDto> getTop10PopularProducts() {
        Set<String> keys = redisTemplate.keys("product:sold_count:*");
        if (keys.isEmpty()) {
            return Collections.emptyList();
        }

        List<ProductSoldCountDto> soldCounts = new ArrayList<>();

        List<String> values = redisTemplate.opsForValue().multiGet(keys);
        int idx = 0;
        for (String key : keys) {
            String value = values.get(idx++);
            if (value != null) {
                UUID productId = UUID.fromString(key.replace("product:sold_count:", ""));
                long soldCount = Long.parseLong(value);
                soldCounts.add(new ProductSoldCountDto(productId, soldCount));
            }
        }

        List<ProductSoldCountDto> top10SoldCounts =  soldCounts.stream()
                .sorted(Comparator.comparingLong(ProductSoldCountDto::soldCount).reversed())
                .limit(10)
                .toList();

        List<PopularProductsResponseDto> responseList = new ArrayList<>();

        for (ProductSoldCountDto soldCountDto : top10SoldCounts) {
            Product product = productService.findProductEntity(soldCountDto.productId());

            responseList.add(new PopularProductsResponseDto(
                    product.getProductId(),
                    product.getName(),
                    product.getPrice(),
                    product.getDescription(),
                    product.getCategory(),
                    soldCountDto.soldCount()
            ));
        }

        return responseList;
    }
}
