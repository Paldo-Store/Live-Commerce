package com.live_commerce.product.inventory.presentation.controller;


import com.live_commerce.product.inventory.application.dto.InventoryDecreaseRequestDto;
import com.live_commerce.product.inventory.application.dto.InventoryIncreaseRequestDto;
import com.live_commerce.product.inventory.application.service.InventoryService;
import com.live_commerce.product.inventory.infrastructure.common.ResponseUtil;
import com.live_commerce.product.inventory.presentation.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products/internal/inventories")
public class InventoryInternalController {

    private final InventoryService inventoryService;

    @PostMapping("/decrease")
    public ResponseEntity<ApiResponse<String>> decreaseInventory(@Valid @RequestBody InventoryDecreaseRequestDto requestDto) {
        inventoryService.decreaseInventory(requestDto.productId(), requestDto.quantity());
        return ResponseUtil.success("재고가 차감되었습니다.");
    }

    @PostMapping("/increase")
    public ResponseEntity<ApiResponse<String>> increaseInventory(@Valid @RequestBody InventoryIncreaseRequestDto requestDto) {
        inventoryService.increaseInventory(requestDto.productId(), requestDto.quantity());
        return ResponseUtil.success("재고가 복원되었습니다.");
    }

}
