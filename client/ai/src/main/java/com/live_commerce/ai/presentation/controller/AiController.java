package com.live_commerce.ai.presentation.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.live_commerce.ai.application.dto.request.AiAnalyzeRequestDto;
import com.live_commerce.ai.application.dto.request.AiSearchCondition;
import com.live_commerce.ai.application.dto.response.AiCreateResponseDto;
import com.live_commerce.ai.application.dto.response.AiGetResponseDto;
import com.live_commerce.ai.application.service.AiService;
import com.live_commerce.ai.infrastructure.common.ResponseUtil;
import com.live_commerce.ai.infrastructure.security.RequestUserDetails;
import com.live_commerce.ai.presentation.common.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiController {

	private final AiService aiService;

	@PostMapping
	public ResponseEntity<ApiResponse<AiCreateResponseDto>> analyze(
		@RequestHeader(value = "X-Internal-Secret", required = false) String secret,
		@RequestBody AiAnalyzeRequestDto request
	) {
		AiCreateResponseDto response = aiService.analyze(request, secret);

		return ResponseUtil.success(response);
	}


	@GetMapping("/{id}")
	@PreAuthorize("hasRole('MASTER')")
	public ResponseEntity<ApiResponse<AiGetResponseDto>> getAiAnalysis(
		@PathVariable UUID id,
		@AuthenticationPrincipal RequestUserDetails requestUserDetails
	) {
		AiGetResponseDto response = aiService.getAiAnalysis(id, requestUserDetails);
		return ResponseUtil.success(response);
	}

	@GetMapping("/search")
	@PreAuthorize("hasRole('MASTER')")
	public ResponseEntity<ApiResponse<Page<AiGetResponseDto>>> getAiAnalysisList(
		@ModelAttribute AiSearchCondition condition,
		Pageable pageable,
		@AuthenticationPrincipal RequestUserDetails requestUserDetails
	) {
		Page<AiGetResponseDto> response = aiService.getAiAnalysisList(condition, pageable, requestUserDetails);
		return ResponseUtil.success(response);
	}


	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('MASTER')")
	public ResponseEntity<ApiResponse<Void>> deleteAiAnalysis(
		@PathVariable UUID id,
		@AuthenticationPrincipal RequestUserDetails requestUserDetails
	) {
		aiService.deleteAiAnalysis(id, requestUserDetails);
		return ResponseUtil.noContent();
	}


}
