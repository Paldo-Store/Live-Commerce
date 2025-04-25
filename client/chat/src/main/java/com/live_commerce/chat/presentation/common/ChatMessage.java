package com.live_commerce.chat.presentation.common;

import com.live_commerce.chat.domain.model.MessageType;

public record ChatMessage(
        MessageType type,
        String roomId,
        String sender,
        String content
) {}
