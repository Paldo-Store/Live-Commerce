package com.live_commerce.ai.infrastructure.client;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.live_commerce.ai.application.dto.response.ChatMessagesResponse;

@FeignClient(name = "chat", path = "/api/v1/chattings")
public interface ChatClient {

	@GetMapping("/search")
	ChatMessagesResponse getChatMessages(@RequestParam("liveBroadcastId") UUID liveBroadcastId);
}
