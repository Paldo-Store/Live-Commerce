package com.live_commerce.order.infrastructure.client;

import com.live_commerce.order.infrastructure.client.response.InventoryCheckQuantityResponseDto;
import com.live_commerce.order.infrastructure.client.response.InventoryCheckResponseDto;
import com.live_commerce.order.presentation.common.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(name = "inventory")
public interface InventoryClient {

    //재고 존재 확인용 -> 실제 주문이 가능한 상태인지를 반환해줌(재고가 있나없나 계산해줌)
    @GetMapping("/api/v1/inventories/checkorderable")
    ApiResponse<InventoryCheckResponseDto> checkOrderableInventory(
            @RequestParam("productId") UUID productId,
            @RequestParam("productQuantity") Integer productQuantity
    );

    //총 재고 개수 들고오기
    @GetMapping("/checkquantity")
    ApiResponse<InventoryCheckQuantityResponseDto> checkInventoryQuantity(
            @RequestParam("productId") UUID productId,
            @RequestParam("productQuantity") Integer productQuantity
    );
}
