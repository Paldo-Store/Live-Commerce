package com.live_commerce.notification.application.service;

import com.live_commerce.notification.domain.model.Notification;
import com.live_commerce.notification.domain.model.NotificationType;
import com.live_commerce.notification.domain.repository.NotificationRepository;
import com.live_commerce.notification.presentation.dto.response.NotificationCreateResponse;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

  private final NotificationRepository notificationRepository;

  // 방송 시작 30분 전에 알림 예약
  public NotificationCreateResponse createNotificationForLiveBroadcast(UUID userId, UUID hostId,
      LocalDateTime notificationTime) {
    Notification notification = Notification.builder()
        .userId(userId)
        .type(NotificationType.LIVE_BROADCAST)
        .targetId(hostId)
        .message("방송이 30분 후에 시작됩니다.")  // 알림 메시지
        .isSent(false)  // 초기 상태는 알림 미전송
        .sentAt(notificationTime) // 알림전송 시간
        .scheduledAt(null)  // 실제 알림 전송 시간
        .build();

    try {
      // 4. 알림 저장
      notificationRepository.save(notification);
      return NotificationCreateResponse.from(notification);
    } catch (Exception e) {
      // 5. 예외 발생 시 로깅 및 예외 던지기
      log.error("알림 저장 중 오류 발생: ", e);
      throw new RuntimeException("알림 저장에 실패했습니다. 다시 시도해주세요.");
    }
  }

  public void sendNotification(UUID notificationId) {
    Notification notification = notificationRepository.findById(notificationId)
        .orElseThrow(() -> new RuntimeException("알림을 찾을 수 없습니다."));

    // 여기에 알림 전송 로직 (슬랙, Firebase, 이메일 등) 추가
    // 예: FirebasePushService.send(notification.getMessage());

    try {
      // 알림 전송 후 알림 상태 업데이트
      Notification updatedNotification = notification.updateIsSent();
      notificationRepository.save(updatedNotification);
    } catch (Exception e) {
      // 알림 상태 업데이트 중 오류 발생
      log.error("알림 상태 업데이트 중 오류 발생: ", e);
      throw new RuntimeException("알림 상태 업데이트에 실패했습니다. 다시 시도해주세요.");
    }
  }

  public Notification getNotification(UUID notificationId) {
    return notificationRepository.findById(notificationId)
        .orElseThrow(() -> new RuntimeException("알림을 찾을 수 없습니다."));
  }
}
