package com.live_commerce.chat.application.dto.message;

import java.util.UUID;

import com.live_commerce.chat.application.dto.request.ChatCreateRequest;

public record ChatRedisPayload(
	UUID userId,
	ChatCreateRequest request
) {}

