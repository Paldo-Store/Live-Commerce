package com.live_commerce.livebroadcast.presentation.controller;

import com.live_commerce.livebroadcast.application.dto.LiveBroadcastCreateRequestDto;
import com.live_commerce.livebroadcast.application.dto.LiveBroadcastCreateResponseDto;
import com.live_commerce.livebroadcast.application.service.LiveBroadcastService;
import com.live_commerce.livebroadcast.infrastructure.common.ResponseUtil;
import com.live_commerce.livebroadcast.presentation.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RefreshScope
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/livebroadcasts")
public class LiveBroadcastController {

    private final LiveBroadcastService liveBroadcastService;

    @PostMapping()
    public ResponseEntity<ApiResponse<LiveBroadcastCreateResponseDto>> createBroadcast(@RequestBody LiveBroadcastCreateRequestDto requestDto) {

        LiveBroadcastCreateResponseDto responseDto = liveBroadcastService.createBroadcast(requestDto);

        return ResponseUtil.success(responseDto);
    }

}
