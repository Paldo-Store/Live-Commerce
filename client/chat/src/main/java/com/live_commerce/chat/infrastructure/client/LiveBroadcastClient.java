package com.live_commerce.chat.infrastructure.client;

import com.live_commerce.chat.application.dto.response.BroadcastResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "livebroadcast")
public interface LiveBroadcastClient {
    @GetMapping("/api/v1/liveBroadcast/{liveBroadcastId}")
    BroadcastResponse getBroadcast(@PathVariable UUID liveBroadcastId);
}
