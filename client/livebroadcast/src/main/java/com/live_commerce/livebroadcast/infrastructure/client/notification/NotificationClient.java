package com.live_commerce.livebroadcast.infrastructure.client.notification;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification", url = "http://localhost:19091", path = "/api/v1/notifications")
public interface NotificationClient {

    @PostMapping("/create-for-broadcast")
    void registerBroadcastAlarm(@RequestBody BroadcastAlarmRegisterRequest request);
}
