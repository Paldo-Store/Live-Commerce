package com.live_commerce.livebroadcast.infrastructure.client.notification;

import java.time.LocalDateTime;
import java.util.UUID;

public record BroadcastAlarmRegisterRequest(
        UUID userId, // 이거 빼 버리고,
        // 알림 타입 넣어라.
        UUID broadcastId, // targetId로 바꾸어라.
        LocalDateTime notificationTime
) { }
