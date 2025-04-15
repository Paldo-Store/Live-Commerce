package com.live_commerce.order.infrastructure.client;

import com.live_commerce.order.application.dto.response.OrderProductResponse;
import com.live_commerce.order.presentation.common.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@FeignClient(name = "product")
public interface ProductClient {

    // 상품 쪽으로 product 실제로 존재하는지 검증 요청  (상품의 개수랑 상품 ID를 같이 넘김)
    // TODO 재고가 없거나 상품이 없다면 Exception
//    @GetMapping("/api/v1/products/validate")
//    OrderProductResponse getProduct(
//            @RequestParam("productId") UUID productId,
//            @RequestParam("quantity") int quantity);
    @GetMapping("/{productId}")
    ApiResponse<OrderProductResponse> getProduct(@PathVariable("productId") UUID productId);


    /**
     * 상품 총 결제 금액 계산
     * @param productId 상품 ID
     * @param quantity 상품 수량
     * @return 총 결제 금액
     */
    //TODO Product에서 총 상품 결제 금액계산 - 수정된 상품 개수로 총 상품 결제 금액 계산
    @GetMapping("/api/v1/products/{productId}/productTotalPrice")
    Long calculateProductTotalPrice(@PathVariable("productId") UUID productId,
                                    @RequestParam("quantity") int quantity);

    // TODO 주문 상태가 결제 완료에서 주문 취소 변경되었을 때, product의 재고 변경
    @PutMapping("/api/v1/products/{productId}/restoreProductQuantity")
    void restoreProductQuantity(
            @PathVariable("productId") UUID productId,
            @RequestParam("quantity") int quantity);

    /**
     * 상품 재고 수량 감소
     * @param productId 상품 ID
     * @param quantity 감소할 수량
     */
    @PostMapping("/products/{productId}/reduce-stock")
    void reduceProductQuantity(@PathVariable("productId") UUID productId,
                               @RequestParam("quantity") int quantity);
}
