package com.live_commerce.livebroadcast.application.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record SubscriptionResponseDto(
        UUID subscriptionId,
        UUID userId,
        UUID broadcastId,
        LocalDateTime createdAt
) {
}
