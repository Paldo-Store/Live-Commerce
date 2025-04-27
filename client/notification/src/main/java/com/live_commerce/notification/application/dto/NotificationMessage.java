package com.live_commerce.notification.application.dto;

import com.live_commerce.notification.domain.model.NotificationType;
import java.time.LocalDateTime;
import java.util.UUID;


public record NotificationMessage (
    UUID notificationId,
    NotificationType type,
    UUID targetId,
    LocalDateTime scheduledAt

){
}