package com.live_commerce.product.inventory.presentation.controller;


import com.live_commerce.product.inventory.application.dto.request.InventoryCreateRequestDto;
import com.live_commerce.product.inventory.application.dto.request.InventoryDecreaseRequestDto;
import com.live_commerce.product.inventory.application.dto.request.InventoryIncreaseRequestDto;
import com.live_commerce.product.inventory.application.dto.response.InventoryCheckQuantityResponseDto;
import com.live_commerce.product.inventory.application.dto.response.InventoryCheckOrderableResponseDto;
import com.live_commerce.product.inventory.application.dto.response.InventoryResponseDto;
import com.live_commerce.product.inventory.application.service.InventoryService;
import com.live_commerce.product.inventory.infrastructure.common.ResponseUtil;
import com.live_commerce.product.inventory.presentation.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inventories")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @PreAuthorize("hasAnyRole('MASTER', 'SELLER')")
    @PostMapping
    public ResponseEntity<ApiResponse<InventoryResponseDto>> createInventory(@RequestBody InventoryCreateRequestDto requestDto) {
        InventoryResponseDto responseDto = inventoryService.createInventory(requestDto);
        return ResponseUtil.success(responseDto);
    }

    @GetMapping("/by-id/{inventoryId}")
    public ResponseEntity<ApiResponse<InventoryResponseDto>> getInventoryById(@PathVariable("inventoryId") UUID inventoryId) {
        InventoryResponseDto responseDto = inventoryService.getInventory(inventoryId);
        return ResponseUtil.success(responseDto);
    }

    @PostMapping("/decrease")
    public ResponseEntity<ApiResponse<String>> decreaseInventory(@Valid @RequestBody InventoryDecreaseRequestDto requestDto) {
        inventoryService.decreaseInventoryWithLua(requestDto.productId(), requestDto.quantity());
        return ResponseUtil.success("재고가 차감되었습니다.");
    }

    @PostMapping("/lua-decrease")
    public ResponseEntity<ApiResponse<String>> decreaseInventoryWithLua(@Valid @RequestBody InventoryDecreaseRequestDto requestDto) {
        inventoryService.decreaseInventoryWithLua(requestDto.productId(), requestDto.quantity());
        return ResponseUtil.success("재고가 차감되었습니다.(lua)");
    }

    @PostMapping("/increase")
    public ResponseEntity<ApiResponse<String>> increaseInventory(@Valid @RequestBody InventoryIncreaseRequestDto requestDto) {
        inventoryService.increaseInventory(requestDto.productId(), requestDto.quantity());
        return ResponseUtil.success("재고가 복원되었습니다.");
    }

    // 재고 총 수량 확인용 - 임시
    @GetMapping("/check-quantity") // get은 파라미터로
    public ResponseEntity<ApiResponse<InventoryCheckQuantityResponseDto>> checkInventoryQuantity(@RequestParam UUID productId) {
        InventoryCheckQuantityResponseDto response = inventoryService.checkInventoryQuantity(productId);
        return ResponseUtil.success(response);
    }

    // 주문 가능 재고 확인용
    @GetMapping("/check-orderable")
    public ResponseEntity<ApiResponse<InventoryCheckOrderableResponseDto>> checkOrderableInventory(@RequestParam UUID productId, @RequestParam int orderQuantity) {
        InventoryCheckOrderableResponseDto response = inventoryService.checkOrderableInventory(productId, orderQuantity);
        return ResponseUtil.success(response);
    }




}
