package com.live_commerce.product.inventory.presentation.controller;

import com.live_commerce.product.inventory.application.dto.request.InventoryDecreaseRequestDto;
import com.live_commerce.product.inventory.infrastructure.common.ResponseUtil;
import com.live_commerce.product.inventory.infrastructure.redisson.test.RedissonLockInventoryTestService;
import com.live_commerce.product.inventory.presentation.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RequiredArgsConstructor
public class InventoryLockTestController {

    private final RedissonLockInventoryTestService redissonLockInventoryTestService;

    @PostMapping("/decrease/no-lock")
    public ResponseEntity<ApiResponse<String>> decreaseInventoryNoLock(@Valid @RequestBody InventoryDecreaseRequestDto requestDto) {
        redissonLockInventoryTestService.decreaseInventoryWithoutLock(requestDto.productId(), requestDto.quantity());
        return ResponseUtil.success("재고가 정상적으로 차감되었습니다.");
    }
}
