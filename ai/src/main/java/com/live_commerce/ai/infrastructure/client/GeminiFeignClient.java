package com.live_commerce.ai.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.live_commerce.ai.application.dto.request.GeminiRequestDto;
import com.live_commerce.ai.application.dto.response.GeminiResponseDto;

@FeignClient(
	name = "geminiClient",
	url = "https://generativelanguage.googleapis.com/v1beta/models"
)
public interface GeminiFeignClient {

	@PostMapping("/{model}:generateContent")
	GeminiResponseDto getCompletion(
		@PathVariable("model") String model,
		@RequestHeader("x-goog-api-key") String key,
		@RequestBody GeminiRequestDto geminiRequestDto
	);
}
