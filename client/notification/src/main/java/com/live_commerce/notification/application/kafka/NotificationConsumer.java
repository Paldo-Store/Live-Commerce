package com.live_commerce.notification.application.kafka;

import com.live_commerce.notification.application.service.NotificationService;
import com.live_commerce.notification.domain.model.Notification;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationConsumer {

  private final NotificationService notificationService;
  private final KafkaTemplate<String, String> kafkaTemplate;

  public void consumerNotification(String notificationId){
    UUID notificationUuid = UUID.fromString(notificationId);

    // 알림 처리
    notificationService.sendNotification(notificationUuid);

    // 알림 정보 카프카에 전송
    Notification notificatrion = notificationService.getNotification(notificationUuid);
    String message  = notificatrion.getMessage();
    String topic = "notification-topic";

    kafkaTemplate.send(topic, message);
  }
}
