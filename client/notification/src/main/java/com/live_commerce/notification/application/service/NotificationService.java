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
import java.util.NoSuchElementException;
import java.util.Optional;
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

    if (notificationRepository.existsByTargetId(request.targetId())) {
      throw new IllegalStateException("해당 알림은 이미 등록되었습니다.");
    }
    Notification notification = Notification.reserve(
        request.notificationType(),
        request.targetId(),
        request.scheduledAt()
    );

    notificationRepository.save(notification);
    log.info("라이브 방송 알림 등록 완료");
    return NotificationCreateResponse.from(notification);
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
      if (!checkType(notification.getType())) {
        continue;
      }
      processNotification(notification);
    }
  }

  private boolean checkType(NotificationType type) {
    return type == NotificationType.LIVE_BROADCAST;
  }

  private void processNotification(Notification notification) {

    List<UserInfo> testUsers = List.of(
        new UserInfo(UUID.randomUUID(), "홍길동"),
        new UserInfo(UUID.randomUUID(), "김개발")
    );

    String broadcastTitle = "봄맞이 특가방송";
    //BroadcastNotificationContext context = broadcastClient.getSubscribersWithTitle(notification.getTargetId());
    BroadcastNotificationContext context = new BroadcastNotificationContext(testUsers);

    boolean success = trySendToAllUsers(notification, context);

    if (success && !notification.isFailed()) {
      notificationRepository.save(notification.markAsSent());
    }
  }

  private boolean trySendToAllUsers(
      Notification notification,
      BroadcastNotificationContext context
  ) {
    // TODO: 통신으로 방송 이름만 가져오거나, requstBody에 포함해서 가져오는 걸로 변경.
    String liveBroadcastName = "테스트 방송";
    boolean allSuccess = true;

    for (UserInfo user : context.users()) {
      try {
        consoleAlertSender.send(user.id(), user.name(), liveBroadcastName);
      } catch (Exception e) {
        allSuccess = false;
        log.warn("⚠️ 사용자 알림 전송 실패: userId={}, msg={}", user.id(), e.getMessage());

        notification = notification.increaseRetryCount();

        if (notification.getRetryCount() >= 5 && !notification.isSent()) {
          notification = notification.markAsFailed();
          notificationRepository.save(notification);
          break;
        }

      }

    }
    return allSuccess;
  }


  public void deleteNotification(UUID targetId) {

    Notification notification = notificationRepository.findByTargetIdAndDeletedStatusFalse(targetId)
        .orElseThrow(() -> new NoSuchElementException("알림이 존재하지 않습니다."));

    notification.markAsDeleted(String.valueOf(notification.getTargetId()));
    notificationRepository.save(notification);
  }
}
