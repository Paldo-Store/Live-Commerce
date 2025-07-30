package com.live_commerce.livebroadcast.application.manager;


import com.live_commerce.livebroadcast.application.validation.NotificationValidator;
import com.live_commerce.livebroadcast.infrastructure.client.notification.BroadcastAlarmRegisterRequest;
import com.live_commerce.livebroadcast.infrastructure.security.RequestUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BroadcastAlarmManager {

    private final NotificationValidator notificationValidator;

    public void register(UUID broadcastId, LocalDateTime startTime) {
        LocalDateTime notifyAt = startTime.minusMinutes(20);

        BroadcastAlarmRegisterRequest request = new BroadcastAlarmRegisterRequest(
                "LIVE_BROADCAST",
                broadcastId,
                notifyAt
        );

        notificationValidator.registerAlarmOrThrow(request);
    }

    public void delete(UUID broadcastId) {
        notificationValidator.deleteAlarmOrLog(broadcastId);
    }
}
