package com.live_commerce.livebroadcast.infrastructure.client.notification;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@FeignClient(name = "notification", url = "http://localhost:19091", path = "/api/v1/notifications")
public interface NotificationClient {

    @PostMapping("/broadcasts")
    void registerBroadcastAlarm(@RequestBody BroadcastAlarmRegisterRequest request);

    @DeleteMapping("/{broadcastId}")
    void unregisterBroadcastAlarm(@PathVariable("broadcastId") UUID broadcastId);

}
