package com.live_commerce.notification.application.service;

import com.live_commerce.notification.application.alert.ConsoleAlertSender;
import com.live_commerce.notification.domain.model.Notification;
import com.live_commerce.notification.domain.model.NotificationType;
import com.live_commerce.notification.domain.repository.NotificationRepository;
import com.live_commerce.notification.presentation.dto.request.BroadcastNotificationContext;
import com.live_commerce.notification.presentation.dto.request.NotificationCreateRequest;
import com.live_commerce.notification.presentation.dto.request.UserInfo;
import com.live_commerce.notification.presentation.dto.response.NotificationCreateResponse;
import com.live_commerce.notification.presentation.dto.response.NotificationResponse;
import com.live_commerce.notification.presentation.dto.response.ReadNotificationListResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

  private final NotificationRepository notificationRepository;

  private final ConsoleAlertSender consoleAlertSender;

  public NotificationCreateResponse createNotificationForLiveBroadcast(
      NotificationCreateRequest request) {

    Notification notification = Notification.reserve(
        request.notificationType(),
        request.targetId(),
        request.scheduledAt()
    );

    notificationRepository.save(notification);
    log.info("라이브 방송 알림 등록 완료");
    return NotificationCreateResponse.from(notification);
  }


  public void sendNotification(String message) {
    // 알림 생성
    Notification notification = Notification.builder()
        .message(message)
        .isSent(true)
        .build();
    notificationRepository.save(notification);
  }

  public ReadNotificationListResponse getAllNotifications() {
    List<Notification> notifications = notificationRepository.findAll();
    List<NotificationResponse> responseList = notifications.stream()
        .map(NotificationResponse::from)
        .toList();
    return new ReadNotificationListResponse(responseList);
  }

  @Scheduled(fixedRate = 60000)
  public void checkScheduledNotifications() {
    LocalDateTime now = LocalDateTime.now();

    List<Notification> toSend = notificationRepository.findAllByScheduledAtLessThanEqualAndIsSentFalse(
        now);

    for (Notification notification : toSend) {

      if (checkType(notification.getType())) {

        List<UserInfo> testUsers = List.of(
            new UserInfo(UUID.randomUUID(), "홍길동"),
            new UserInfo(UUID.randomUUID(), "김개발")
        );

        String broadcastTitle = "봄맞이 특가방송";

//        BroadcastNotificationContext context = broadcastClient.getSubscribersWithTitle(notification.getTargetId());
        BroadcastNotificationContext context = new BroadcastNotificationContext(testUsers,
            broadcastTitle);

        for (UserInfo user : context.users()) {
          String msg = "[TEST]" + user.name() + "님, \"" + context.liveBroadcastName()
              + "\" 방송이 30분 후 시작됩니다.";
          consoleAlertSender.send(user.id(), msg);
        }
      }
      notificationRepository.save(notification.markAsSent());
    }

  }

  private boolean checkType(NotificationType type) {
    return type == NotificationType.LIVE_BROADCAST;
  }

}
