package com.live_commerce.product.inventory.presentation.controller;


import com.live_commerce.product.inventory.application.dto.InventoryCreateRequestDto;
import com.live_commerce.product.inventory.application.dto.InventoryResponseDto;
import com.live_commerce.product.inventory.application.service.InventoryService;
import com.live_commerce.product.inventory.infrastructure.common.ResponseUtil;
import com.live_commerce.product.inventory.presentation.common.ApiResponse;
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

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InventoryResponseDto>> getInventoryById(@PathVariable UUID id) {
        InventoryResponseDto responseDto = inventoryService.getInventory(id);
        return ResponseUtil.success(responseDto);
    }
}
