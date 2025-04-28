package com.live_commerce.notification.infrastructure.kafka.producer;

import com.live_commerce.notification.application.dto.NotificationMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationProducer {

  private static final String TOPIC = "notification-topic";
  private final KafkaTemplate<String, NotificationMessage> kafkaTemplate;

  public NotificationProducer(final KafkaTemplate<String, NotificationMessage> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  public void send (NotificationMessage msg) {
    kafkaTemplate.send(TOPIC, msg.notificationId().toString(), msg);
    log.info("📤 [{}] 메시지 발행: {}", TOPIC, msg);
  }
}