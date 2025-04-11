package com.live_commerce.livebroadcast.presentation.controller;

import com.live_commerce.livebroadcast.application.dto.request.BroadcastProductConnectDto;
import com.live_commerce.livebroadcast.application.dto.response.BroadcastProductResponseDto;
import com.live_commerce.livebroadcast.application.service.BroadcastProductService;
import com.live_commerce.livebroadcast.domain.model.BroadcastProduct;
import com.live_commerce.livebroadcast.domain.repository.BroadcastProductRepository;
import com.live_commerce.livebroadcast.infrastructure.common.ResponseUtil;
import com.live_commerce.livebroadcast.presentation.common.ApiResponse;
import lombok.RequiredArgsConstructor;
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


}
