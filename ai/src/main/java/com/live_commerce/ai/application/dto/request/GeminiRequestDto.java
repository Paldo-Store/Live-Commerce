package com.live_commerce.ai.application.dto.request;

import java.util.List;

public record GeminiRequestDto(
	List<Content> contents
) {
	public record Content(List<TextPart> parts) {}

	public record TextPart(String text) {}
}
