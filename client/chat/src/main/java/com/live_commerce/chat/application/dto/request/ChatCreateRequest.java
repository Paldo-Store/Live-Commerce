package com.live_commerce.chat.application.dto.request;

import com.live_commerce.chat.domain.model.MessageType;

import java.util.UUID;

public record ChatCreateRequest (
        UUID userId,
        String chatting,
        UUID liveBroadcastId,
        MessageType messageType
){}
