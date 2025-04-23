package com.live_commerce.livebroadcast.infrastructure.client.notification;

import java.time.LocalDateTime;
import java.util.UUID;

public record BroadcastAlarmRegisterRequest(
        String notificationType, // LIVE_BROADCAST 고정
        UUID targetId, // 방송id
        LocalDateTime scheduledAt
) { }
