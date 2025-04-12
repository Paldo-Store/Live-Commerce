package com.live_commerce.livebroadcast.presentation.controller;

import com.live_commerce.livebroadcast.application.dto.request.BroadcastProductConnectDto;
import com.live_commerce.livebroadcast.application.dto.response.BroadcastProductListResponseDto;
import com.live_commerce.livebroadcast.application.dto.response.BroadcastProductResponseDto;
import com.live_commerce.livebroadcast.application.dto.response.ProductPageResponse;
import com.live_commerce.livebroadcast.application.service.BroadcastProductService;
import com.live_commerce.livebroadcast.infrastructure.common.ResponseUtil;
import com.live_commerce.livebroadcast.presentation.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/livebroadcasts/{broadcastId}/products")
@RequiredArgsConstructor
public class BroadcastProductController {

    private final BroadcastProductService broadcastProductService;

    @PostMapping
    public ResponseEntity<ApiResponse<BroadcastProductResponseDto>> connectBroadcastProduct(
            @RequestBody BroadcastProductConnectDto requestDto,
            @PathVariable UUID broadcastId
    ) {
        BroadcastProductResponseDto responseDto = broadcastProductService.connectBroadcastProduct(broadcastId,requestDto);
        return ResponseUtil.success(responseDto);
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<String>> disconnectBroadcastProduct(
            @PathVariable UUID productId,
            @PathVariable UUID broadcastId) {
        broadcastProductService.disconnectBroadcastProduct(broadcastId, productId);
        return ResponseUtil.success("해당 방송과 연결된 상품을 해제하였습니다.");
    }

    @GetMapping("")
    public ResponseEntity<ApiResponse<ProductPageResponse>> getProducts(
            @PathVariable UUID broadcastId,
            Pageable pageable
    ) {
        ProductPageResponse response = broadcastProductService.getBroadcastProducts(broadcastId, pageable);
        return ResponseUtil.success(response);
    }


}
