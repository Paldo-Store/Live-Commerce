package com.live_commerce.notification.infrastructure.kafka.consumer;

import com.live_commerce.notification.infrastructure.kafka.event.NotificationCreatedEvent;
import com.live_commerce.notification.application.service.NotificationService;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationEventConsumer {

  private final NotificationService notificationService;

  public NotificationEventConsumer(NotificationService notificationService) {
    this.notificationService = notificationService;
  }

  @KafkaListener(
      topics = "notification-created",
      groupId = "${spring.application.name}",
      containerFactory = "kafkaListenerContainerFactory"
  )
  public void listen(NotificationCreatedEvent msg) throws IOException {
    notificationService.processByMessage(msg);
  }
}