package com.live_commerce.notification.infrastructure.kafka.consumer;

import com.live_commerce.notification.application.dto.NotificationMessage;
import com.live_commerce.notification.application.service.NotificationService;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationConsumer {

  private final NotificationService notificationService;

  public NotificationConsumer(NotificationService notificationService) {
    this.notificationService = notificationService;
  }

  @KafkaListener(
      topics = "notification-topic",
      groupId = "notification-group",
      containerFactory = "kafkaListenerContainerFactory"
  )
  public void listen(NotificationMessage msg) throws IOException {
    notificationService.processByMessage(msg);
  }
}