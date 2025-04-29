package com.live_commerce.notification.infrastructure.kafka.producer;

import com.live_commerce.notification.infrastructure.kafka.event.NotificationCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationEventProducer {

  private static final String TOPIC = "notification-created";
  private final KafkaTemplate<String, NotificationCreatedEvent> kafkaTemplate;

  public NotificationEventProducer(final KafkaTemplate<String, NotificationCreatedEvent> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  public void sendNotificationCreated (NotificationCreatedEvent event) {
    kafkaTemplate.send(TOPIC, event.notificationId().toString(), event);
    log.info("📤 [{}] 메시지 발행: {}", TOPIC, event);
  }
}