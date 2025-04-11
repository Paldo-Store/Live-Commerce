package com.live_commerce.ai.application.dto.response;

import java.util.List;

public record ChatMessagesResponse(
	List<ChatMessage> content
) {
	public record ChatMessage(String message) {}
}
