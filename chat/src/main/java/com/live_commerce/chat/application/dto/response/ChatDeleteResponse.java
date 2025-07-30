package com.live_commerce.chat.application.dto.response;

import com.live_commerce.chat.domain.model.Chat;

import java.util.UUID;

public record ChatDeleteResponse(UUID chatId, String message) {

    public static ChatDeleteResponse of(Chat chat) {
        return new ChatDeleteResponse(chat.getId(), "채팅이 삭제되었습니다.");
    }
}