package com.live_commerce.ai.infrastructure.client;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import com.live_commerce.ai.application.dto.request.GeminiRequestDto;
import com.live_commerce.ai.application.dto.response.GeminiResponseDto;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class GeminiServiceAdapter {

	private final GeminiFeignClient geminiFeignClient;

	@Value("${gemini.api.key}")
	private String apiKey;

	@Value("${gemini.model.name}")
	private String modelName;

	@Retryable(
		retryFor = { FeignException.TooManyRequests.class, FeignException.ServiceUnavailable.class },
		maxAttempts = 3,
		backoff = @Backoff(delay = 1000, multiplier = 2)
	)
	public String generateText(String prompt) {
		GeminiRequestDto requestDto = new GeminiRequestDto(
			List.of(new GeminiRequestDto.Content(
				List.of(new GeminiRequestDto.TextPart(prompt))
			))
		);

		GeminiResponseDto responseDto = geminiFeignClient.getCompletion(modelName, apiKey, requestDto);
		String text = responseDto.extractText();
		if (text.isEmpty()) {
			log.warn("[Gemini] 응답 본문이 비어있음: model={}", modelName);
		}
		return text;
	}

	// 재시도 소진 후 호출 — 로그 남기고 rethrow, AiService가 CustomException으로 변환
	@Recover
	public String recoverGenerateText(FeignException e, String prompt) {
		log.warn("[Gemini] 재시도 소진 - API 호출 실패: status={}, message={}", e.status(), e.getMessage());
		throw e;
	}
}
