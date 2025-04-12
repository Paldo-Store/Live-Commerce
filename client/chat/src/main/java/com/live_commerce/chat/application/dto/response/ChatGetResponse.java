package com.live_commerce.chat.application.dto.response;

import com.live_commerce.chat.domain.model.Chat;
import org.springframework.data.domain.Page;

import java.util.List;

public record ChatGetResponse(List<ChatCreateResponse> companies, int totalPages,
                              long totalElements) {
    public static ChatGetResponse of(Page<Chat> chatPages) {
        return new ChatGetResponse(chatPages.map(ChatCreateResponse::of).toList(),
                chatPages.getTotalPages(), chatPages.getTotalElements());
    }
}