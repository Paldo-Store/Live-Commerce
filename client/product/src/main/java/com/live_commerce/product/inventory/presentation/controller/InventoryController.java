package com.live_commerce.product.inventory.presentation.controller;


import com.live_commerce.product.inventory.application.dto.InventoryCreateRequestDto;
import com.live_commerce.product.inventory.application.dto.InventoryDecreaseRequestDto;
import com.live_commerce.product.inventory.application.dto.InventoryIncreaseRequestDto;
import com.live_commerce.product.inventory.application.dto.InventoryResponseDto;
import com.live_commerce.product.inventory.application.service.InventoryService;
import com.live_commerce.product.inventory.infrastructure.common.ResponseUtil;
import com.live_commerce.product.inventory.presentation.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inventories")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping
    public ResponseEntity<ApiResponse<InventoryResponseDto>> createInventory(@RequestBody InventoryCreateRequestDto requestDto) {
        InventoryResponseDto responseDto = inventoryService.createInventory(requestDto);
        return ResponseUtil.success(responseDto);
    }

    @GetMapping("/{inventoryId}")
    public ResponseEntity<ApiResponse<InventoryResponseDto>> getInventoryById(@PathVariable UUID inventoryId) {
        InventoryResponseDto responseDto = inventoryService.getInventory(inventoryId);
        return ResponseUtil.success(responseDto);
    }

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
