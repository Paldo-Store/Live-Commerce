package com.live_commerce.order.infrastructure.client;

import com.live_commerce.order.application.dto.response.OrderProductResponse;
import com.live_commerce.order.infrastructure.client.response.InventoryCheckQuantityResponseDto;
import com.live_commerce.order.infrastructure.client.response.InventoryCheckResponseDto;
import com.live_commerce.order.presentation.common.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@FeignClient(name = "product" , url = "http://localhost:19091")
public interface ProductClient {

    // 상품 쪽으로 product 실제로 존재하는지 검증 요청  (상품 ID를 같이 넘김)
    @GetMapping("/api/v1/products/{productId}")
    ApiResponse<ProductCreateResponseDto> getProduct(@PathVariable("productId") UUID productId);

    //결제 취소시 재고 복구
    @PostMapping("/api/v1/inventories/increase")
    ResponseEntity<ApiResponse<String>> increaseInventory(@RequestBody InventoryIncreaseRequestDto requestDto);

    //결제 승인시 재고 감소
    @PostMapping("/api/v1/inventories/decrease")
    ResponseEntity<ApiResponse<String>> decreaseInventory(@RequestBody InventoryDecreaseRequestDto requestDto);

    //재고 존재 확인용 -> 실제 주문이 가능한 상태인지를 반환해줌(재고가 있나없나 계산해줌)
    @GetMapping("/api/v1/inventories/check-orderable")
    ApiResponse<InventoryCheckResponseDto> checkOrderableInventory(
            @RequestParam("productId") UUID productId,
            @RequestParam("orderQuantity") int orderQuantity
    );

    //총 재고 개수 들고오기
    @GetMapping("/api/v1/inventories/check-quantity")
    ApiResponse<InventoryCheckQuantityResponseDto> checkInventoryQuantity(
            @RequestParam("productId") UUID productId,
            @RequestParam("orderQuantity") int orderQuantity
    );
}
