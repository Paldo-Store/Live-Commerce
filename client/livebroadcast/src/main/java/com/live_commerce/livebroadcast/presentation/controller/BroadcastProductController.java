package com.live_commerce.livebroadcast.presentation.controller;

import com.live_commerce.livebroadcast.application.dto.request.BroadcastProductConnectDto;
import com.live_commerce.livebroadcast.application.dto.response.BroadcastProductResponseDto;
import com.live_commerce.livebroadcast.application.dto.response.BroadcastProductPageResponse;
import com.live_commerce.livebroadcast.application.service.BroadcastProductService;
import com.live_commerce.livebroadcast.infrastructure.common.ResponseUtil;
import com.live_commerce.livebroadcast.presentation.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
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
        System.out.println("🚀 컨트롤러 진입: broadcastId=" + broadcastId + ", productId=" + requestDto.productId());
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
    public ResponseEntity<ApiResponse<BroadcastProductPageResponse>> getProducts(
            @PathVariable UUID broadcastId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        BroadcastProductPageResponse response = broadcastProductService.getBroadcastProducts(broadcastId, pageable);
        return ResponseUtil.success(response);
    }

    /**
     * 특정 방송에 특정 상품이 연결되어 있는지 확인하는 API입니다.
     * <p>
     * 주문 서비스 등 외부 서비스에서 방송-상품 연결 여부를 검증할 때 사용됩니다.
     * 소프트 삭제된 방송 및 연결은 제외됩니다.
     */
    @GetMapping("/{productId}/exists")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkProductExists(
            @PathVariable UUID broadcastId,
            @PathVariable UUID productId
    ) {
        boolean exists = broadcastProductService.existsByBroadcastIdAndProductId(broadcastId, productId);
        return ResponseUtil.success(Map.of("exists", exists));
    }

}
