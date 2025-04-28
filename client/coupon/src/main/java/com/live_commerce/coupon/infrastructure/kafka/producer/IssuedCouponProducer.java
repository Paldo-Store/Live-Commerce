package com.live_commerce.coupon.infrastructure.kafka.producer;

import com.live_commerce.coupon.application.port.IssueFirstJoinCouponPort;
import com.live_commerce.coupon.infrastructure.config.KafkaConfig;
import com.live_commerce.coupon.infrastructure.kafka.dto.FirstJoinCouponMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class IssuedCouponProducer implements IssueFirstJoinCouponPort {

  private final KafkaTemplate<String, Object> kafkaTemplate;

  @Override
  public void publishFirstJoinEvent(UUID userId) {
    FirstJoinCouponMessage msg = new FirstJoinCouponMessage(userId);
    kafkaTemplate.send(KafkaConfig.FIRST_COUPON_TOPIC, userId.toString(), msg);
  }
}