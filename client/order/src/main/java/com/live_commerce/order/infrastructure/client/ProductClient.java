package com.live_commerce.order.infrastructure.client;

import com.live_commerce.order.application.dto.response.OrderProductResponse;
import com.live_commerce.order.presentation.common.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@FeignClient(name = "product")
public interface ProductClient {

    // 상품 쪽으로 product 실제로 존재하는지 검증 요청  (상품 ID를 같이 넘김)
    @GetMapping("/{productId}")
    ApiResponse<OrderProductResponse> getProduct(@PathVariable("productId") UUID productId);

    //결제 취소시 재고 복구
    @PostMapping("/increase")
    ResponseEntity<ApiResponse<String>> increaseInventory(@RequestBody InventoryIncreaseRequestDto requestDto);

    //결제 승인시 재고 감소
    @PostMapping("/decrease")
    ResponseEntity<ApiResponse<String>> decreaseInventory(@RequestBody InventoryDecreaseRequestDto requestDto);
}
