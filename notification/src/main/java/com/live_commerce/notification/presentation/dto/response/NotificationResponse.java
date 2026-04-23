package com.live_commerce.notification.presentation.dto.response;

import com.live_commerce.notification.domain.model.Notification;
import com.live_commerce.notification.domain.model.NotificationType;
import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationResponse(
    UUID id,
    NotificationType type,
    UUID targetId,
    String message,
    boolean isSent,
    LocalDateTime sentAt,
    LocalDateTime scheduledAt
) {
  public static NotificationResponse from(Notification notification) {
    return new NotificationResponse(
        notification.getId(),
        notification.getType(),
        notification.getTargetId(),
        notification.getMessage(),
        notification.isSent(),
        notification.getSentAt(),
        notification.getScheduledAt()
    );
  }
}

