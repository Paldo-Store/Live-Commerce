package com.live_commerce.ai.infrastructure.client;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.live_commerce.ai.application.dto.request.GeminiRequestDto;
import com.live_commerce.ai.application.dto.response.GeminiResponseDto;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GeminiServiceAdapter {

	private final GeminiFeignClient geminiFeignClient;

	@Value("${gemini.api.key}")
	private String apiKey;

	@Value("${gemini.model.name}")
	private String modelName;

	public String generateText(String prompt) {
		GeminiRequestDto requestDto = new GeminiRequestDto(
			List.of(new GeminiRequestDto.Content(
				List.of(new GeminiRequestDto.TextPart(prompt))
			))
		);

		GeminiResponseDto responseDto = geminiFeignClient.getCompletion(modelName, apiKey, requestDto);
		return responseDto.extractText();
	}
}
