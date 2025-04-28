package com.live_commerce.product.inventory;

import com.live_commerce.product.inventory.application.service.ProductRankingService;
import com.live_commerce.product.product.application.dto.PopularProductsResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("ProductRankingService 인기상품 조회 테스트")
public class ProductRankingServiceTest {

    @Autowired
    private ProductRankingService productRankingService;

    @Autowired
    private StringRedisTemplate redisTemplate;


    @Test
    @DisplayName("Redis에 판매량이 저장되어 있을 때 Top 10 인기상품을 조회할 수 있다.")
    void getTop10PopularProducts() {
        // given
        List<UUID> dummyProductIds = List.of(
                UUID.fromString("30000000-0000-0000-0000-000000000007"),
                UUID.fromString("30000000-0000-0000-0000-000000000009"),
                UUID.fromString("30000000-0000-0000-0000-00000000000a"),
                UUID.fromString("30000000-0000-0000-0000-00000000000c"),
                UUID.fromString("30000000-0000-0000-0000-00000000000d"),
                UUID.fromString("30000000-0000-0000-0000-000000000001"),
                UUID.fromString("30000000-0000-0000-0000-000000000003"),
                UUID.fromString("30000000-0000-0000-0000-000000000004"),
                UUID.fromString("30000000-0000-0000-0000-000000000006"),
                UUID.fromString("30000000-0000-0000-0000-000000000013")
        );

        int baseSoldCount = 100;
        for (UUID productId : dummyProductIds) {
            String key = "product:sold_count:" + productId;
            // 판매량을 다르게 세팅
            redisTemplate.opsForValue().set(key, String.valueOf(baseSoldCount++));
        }

        // when
        List<PopularProductsResponseDto> top10Products = productRankingService.getTop10PopularProducts();

        // then
        assertThat(top10Products).hasSize(10);
        assertThat(top10Products).isSortedAccordingTo(
                Comparator.comparingLong(PopularProductsResponseDto::soldCount).reversed()
        );

        top10Products.forEach(product ->
                System.out.println("상품 ID: " + product.productId() +
                        ", 상품 이름: " + product.name() +
                        ", 판매량: " + product.soldCount())
        );
    }
}
