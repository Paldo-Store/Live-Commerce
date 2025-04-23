package com.live_commerce.order.infrastructure.client;

import com.live_commerce.order.infrastructure.client.response.BroadcastStatusResponse;
import com.live_commerce.order.presentation.common.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "livebroadcast", url = "http://localhost:19091", path = "/api/v1/livebroadcasts")
public interface BroadcastClient {

    // 방송 조회 feign 요청
    @GetMapping("/{broadcastId}")
    ApiResponse<BroadcastStatusResponse> getBroadcast(@PathVariable("broadcastId") UUID broadcastId);
}