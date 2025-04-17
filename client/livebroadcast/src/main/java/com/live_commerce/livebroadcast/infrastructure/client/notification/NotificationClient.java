package com.live_commerce.livebroadcast.infrastructure.client.notification;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@FeignClient(name = "notification", url = "http://localhost:19091", path = "/api/v1/notifications")
public interface NotificationClient {

    @PostMapping("/create-for-broadcast")
    void registerBroadcastAlarm(@RequestBody BroadcastAlarmRegisterRequest request);

    // 방송 스케줄 변경 시 알림도 수정
    @PutMapping("/{broadcastId}")
    void updateBroadcastAlarm(@PathVariable("broadcastId") UUID broadcastId, @RequestBody BroadcastAlarmRegisterRequest request);

    // 방송 삭제 시 알림 삭제
    @DeleteMapping("/{broadcastId}")
    void unregisterBroadcastAlarm(@PathVariable("broadcastId") UUID broadcastId);


}
