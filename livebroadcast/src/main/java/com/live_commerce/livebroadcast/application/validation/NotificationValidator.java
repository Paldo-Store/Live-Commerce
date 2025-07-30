package com.live_commerce.livebroadcast.application.validation;

import com.live_commerce.livebroadcast.domain.exception.LiveBroadcastException;
import com.live_commerce.livebroadcast.infrastructure.client.notification.BroadcastAlarmRegisterRequest;
import com.live_commerce.livebroadcast.infrastructure.client.notification.NotificationClient;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationValidator {

    private final NotificationClient notificationClient;

    /**
     * 알림 등록 요청, 실패 시 예외 발생
     */
    public void registerAlarmOrThrow(BroadcastAlarmRegisterRequest request) {
        try {
            notificationClient.registerBroadcastAlarm(request);
        } catch (FeignException.BadRequest e) {
            log.warn("잘못된 알림 등록 요청: request={}, message={}", request, e.getMessage());
            throw LiveBroadcastException.forInvalidAlarmRequest();
        } catch (FeignException e) {
            log.warn("알림 등록 실패: request={}, message={}", request, e.getMessage());
            throw new RuntimeException("알림 등록 실패", e);
        }
    }


    /**
     * 알림 삭제 요청, 실패 시 로그만 출력
     */
    public void deleteAlarmOrLog(UUID broadcastId) {
        try {
            notificationClient.unregisterBroadcastAlarm(broadcastId);
        } catch (FeignException e) {
            log.warn("알림 삭제 실패: broadcastId={}, message={}", broadcastId, e.getMessage());
        }
    }

}
