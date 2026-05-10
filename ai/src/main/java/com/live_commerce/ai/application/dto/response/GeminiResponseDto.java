package com.live_commerce.ai.application.dto.response;

import java.util.List;

public record GeminiResponseDto(List<Candidate> candidates) {
	public String extractText() {
		if (candidates == null || candidates.isEmpty()) return "";
		Content content = candidates.get(0).content();
		if (content == null) return "";
		List<Content.Part> parts = content.parts();
		return (parts != null && !parts.isEmpty()) ? parts.get(0).text() : "";
	}

	public record Candidate(Content content) {}

	public record Content(List<Part> parts) {
		public record Part(String text) {}
	}
}
