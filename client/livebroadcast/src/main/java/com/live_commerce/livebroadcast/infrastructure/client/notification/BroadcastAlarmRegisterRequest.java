package com.live_commerce.livebroadcast.infrastructure.client.notification;

import java.time.LocalDateTime;
import java.util.UUID;

public record BroadcastAlarmRegisterRequest(
        String notificationType,
        UUID targetId, // 방송id
        LocalDateTime notificationTime
) { }
