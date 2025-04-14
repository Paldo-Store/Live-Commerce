package com.live_commerce.ai.application.dto.response;

import java.util.UUID;

import com.live_commerce.ai.domain.model.AI;

public record AiCreateResponseDto(
	UUID id,
	UUID liveBroadcastId,
	String requestPayload,
	String responsePayload
) {
	public static AiCreateResponseDto from(AI ai) {
		return new AiCreateResponseDto(
			ai.getId(),
			ai.getLiveBroadcastId(),
			ai.getRequestPayload(),
			ai.getResponsePayload()
		);
	}
}
