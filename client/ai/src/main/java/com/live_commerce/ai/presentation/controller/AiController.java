package com.live_commerce.ai.presentation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.live_commerce.ai.application.dto.request.AiRequestDto;
import com.live_commerce.ai.application.dto.response.AiResponseDto;
import com.live_commerce.ai.application.service.AiService;
import com.live_commerce.ai.infrastructure.common.ResponseUtil;
import com.live_commerce.ai.presentation.common.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiController {

	private final AiService aiService;

	@PostMapping
	public ResponseEntity<ApiResponse<AiResponseDto>> analyze(@RequestBody AiRequestDto request) {
		AiResponseDto response = aiService.analyze(request);
		return ResponseUtil.success(response);
	}
}
