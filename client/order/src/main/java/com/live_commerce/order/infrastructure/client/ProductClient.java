package com.live_commerce.order.infrastructure.client;

import com.live_commerce.order.application.dto.response.OrderProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(name = "product")
public interface ProductClient {

    // 상품 쪽으로 검증 요청  (상품의 개수랑 상품 ID를 같이 넘김)
    // 재고가 없거나 상품이 없다면 Exception
    @GetMapping("/api/v1/products/validate")
    OrderProductResponse getProduct(
            @RequestParam("productId") UUID productId,
            @RequestParam("quantity") int quantity);

    // 주문 상태가 결제 완료에서 주문 취소 변경되었을 때, product의 재고 변경
    @PutMapping("/api/v1/products/{productId}/restore-stock")
    void updateProductState(
            @PathVariable("productId") UUID productId,
            @RequestParam("quantity") int quantity);
}
