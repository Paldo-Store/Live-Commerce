package com.live_commerce.notification.application.kafka;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationProducer {

  private final KafkaTemplate<String, String> kafkaTemplate;

  private static final String NOTIFICATION_TOPIC  = "notification-topic";

  public void sendNotification(UUID notificationId){
    String message = notificationId.toString();
    kafkaTemplate.send(NOTIFICATION_TOPIC, message);
  }
}
