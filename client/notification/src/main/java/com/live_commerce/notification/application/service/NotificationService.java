package com.live_commerce.notification.application.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.live_commerce.notification.application.alert.AlertSender;
import com.live_commerce.notification.application.alert.ConsoleAlertSender;
import com.live_commerce.notification.application.dto.NotificationMessage;
import com.live_commerce.notification.domain.model.Notification;
import com.live_commerce.notification.domain.model.NotificationType;
import com.live_commerce.notification.domain.repository.NotificationRepository;
import com.live_commerce.notification.infrastructure.kafka.producer.NotificationProducer;
import com.live_commerce.notification.presentation.dto.request.BroadcastNotificationContext;
import com.live_commerce.notification.presentation.dto.request.NotificationCreateRequest;
import com.live_commerce.notification.presentation.dto.request.UserInfo;
import com.live_commerce.notification.presentation.dto.response.NotificationCreateResponse;
import com.live_commerce.notification.presentation.dto.response.NotificationResponse;
import com.live_commerce.notification.presentation.dto.response.ReadNotificationListResponse;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

  private final NotificationRepository notificationRepository;

  private final ConsoleAlertSender consoleAlertSender;
  private final NotificationProducer producer;
  private final ObjectMapper objectMapper;
  private final AlertSender alertSender;

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

  // users.json 파일에서 사용자 리스트를 읽어오기
  public List<UserInfo> getAllUsersFromJson() throws IOException {
    File file = new File(getClass().getClassLoader().getResource("data/users.json").getFile());
    return objectMapper.readValue(file, new TypeReference<List<UserInfo>>() {
    });
  }

  // 테스트용으로 직접 호출할 수 있도록 하는 메서드 추가
  public void triggerScheduledNotifications() throws IOException {
    checkScheduledNotifications();
  }

  //  @Scheduled(fixedRate = 60000)
  public void checkScheduledNotifications() throws IOException {
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

  private void processNotification(Notification notification) throws IOException {
    List<UserInfo> testUsers = getAllUsersFromJson();
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

  // Kafka 도입
  public void triggerKafkaNotifications() throws IOException {
    publishScheduledNotifications();
  }

  //  @Scheduled(fixedDelay = 60_000)
  public void publishScheduledNotifications() {
    List<Notification> list = notificationRepository.findAllByScheduledAtLessThanEqualAndIsSentFalse(
        LocalDateTime.now());
    for (Notification n : list) {
      NotificationMessage msg = new NotificationMessage(
          n.getId(), n.getType(), n.getTargetId(), n.getScheduledAt()
      );
      producer.send(msg);
    }
  }

  public void processByMessage(NotificationMessage msg) throws IOException {
    Notification notification = notificationRepository.findById(msg.notificationId())
        .orElseThrow(() -> new IllegalArgumentException("알림 없음: " + msg.notificationId()));

    if (notification.isSent()) {
      return;
    }

    List<UserInfo> users = getAllUsersFromJson();

    boolean allSuccess = true;
    for (UserInfo user : users) {
      try {
        alertSender.send(user.id(), user.name(), "kafka messsage 테스트");
      } catch (Exception e) {
        allSuccess = false;
        log.warn("⚠️ 알림 전송 실패: userId={}, {}", user.id(), e.getMessage());
      }
    }
    if (allSuccess) {
      notification = notification.markAsSent();
    }
    notificationRepository.save(notification);
  }
}
