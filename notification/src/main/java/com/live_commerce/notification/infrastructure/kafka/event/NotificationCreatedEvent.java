package com.live_commerce.notification.infrastructure.kafka.event;

import com.live_commerce.notification.domain.model.NotificationType;
import java.time.LocalDateTime;
import java.util.UUID;


public record NotificationCreatedEvent(
    UUID notificationId,
    NotificationType type,
    UUID targetId,
    LocalDateTime scheduledAt

){
}