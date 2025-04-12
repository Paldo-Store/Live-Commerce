package com.live_commerce.chat.application.dto.response;

import com.live_commerce.chat.domain.model.Chat;

import java.util.UUID;

public record ChatCreateResponse(
        UUID id,
        UUID userId,
        String chatting
) {
    public static ChatCreateResponse of(Chat chat) {
        return new ChatCreateResponse(
                chat.getId(),
                chat.getUserId(),
                chat.getChatting()
        );
    }
}
