package com.live_commerce.ai.application.dto.response;

import java.util.UUID;

import com.live_commerce.ai.domain.model.AI;

public record AiResponseDto(
	UUID id,
	UUID liveBroadcastId,
	String requestPayload,
	String responsePayload
) {
	public static AiResponseDto from(AI ai) {
		return new AiResponseDto(
			ai.getId(),
			ai.getLiveBroadcastId(),
			ai.getRequestPayload(),
			ai.getResponsePayload()
		);
	}
}
