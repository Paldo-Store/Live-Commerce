package com.live_commerce.chat.application.dto.request;

import java.util.UUID;

public record ChatCreateRequest (
        UUID userId,
        String chatting
){}
