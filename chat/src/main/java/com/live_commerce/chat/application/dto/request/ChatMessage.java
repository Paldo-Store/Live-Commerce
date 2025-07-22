package com.live_commerce.chat.application.dto.request;

import com.live_commerce.chat.domain.model.MessageType;

import java.util.UUID;

public record ChatMessage(
        UUID userId,
        String chatting,
        UUID liveBroadcastId,  // 방송 ID
        MessageType type
) {}