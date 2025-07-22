package com.live_commerce.ai.application.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.live_commerce.ai.domain.model.AI;

public record AiGetResponseDto(
	UUID id,
	UUID liveBroadcastId,
	String requestPayload,
	String responsePayload,
	LocalDateTime createdAt,
	LocalDateTime updatedAt,
	String createdBy,
	String updatedBy
) {
	public static AiGetResponseDto from(AI ai) {
		return new AiGetResponseDto(
			ai.getId(),
			ai.getLiveBroadcastId(),
			ai.getRequestPayload(),
			ai.getResponsePayload(),
			ai.getCreatedAt(),
			ai.getUpdatedAt(),
			ai.getCreatedBy(),
			ai.getUpdatedBy()
		);
	}
}

