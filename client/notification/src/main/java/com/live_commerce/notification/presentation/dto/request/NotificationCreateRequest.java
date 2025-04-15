package com.live_commerce.notification.presentation.dto.request;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationCreateRequest(
    UUID userId,
    UUID broadcastId,
    LocalDateTime notificationTime
) {
}
