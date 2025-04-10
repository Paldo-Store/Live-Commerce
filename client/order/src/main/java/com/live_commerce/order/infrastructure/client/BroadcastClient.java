package com.live_commerce.order.infrastructure.client;

import com.live_commerce.order.infrastructure.client.response.BroadcastStatusResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "livebroadcast")
public interface BroadcastClient {

    @GetMapping("/api/v1/broadcasts/{broadcastId}/status")
    BroadcastStatusResponse getBroadcastStatus(@PathVariable UUID broadcastId);
}