package com.live_commerce.ai.application.dto.request;

import java.util.List;
import java.util.UUID;

public record AiAnalyzeRequestDto(
	UUID live_broadcast_id,
	RequestPayload request_payload
) {
	public record RequestPayload(
		List<ChatMessage> chat_messages
	) {}

	public record ChatMessage(
		String message
	) {}
}
