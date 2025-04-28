package com.live_commerce.coupon.infrastructure.kafka.producer;

import com.live_commerce.coupon.application.port.PublishCouponUsedEventPort;
import com.live_commerce.coupon.infrastructure.config.KafkaConfig;
import com.live_commerce.coupon.infrastructure.kafka.dto.CouponUsedMessage;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CouponUsedProducer implements PublishCouponUsedEventPort {

  private final KafkaTemplate<String, Object> kafkaTemplate;

  @Override
  public void publishCouponUsedEvent(UUID couponId, UUID userId) {
    CouponUsedMessage msg = new CouponUsedMessage(couponId, userId);

    kafkaTemplate.send(
        KafkaConfig.COUPON_USED_TOPIC,
        couponId.toString(),
        msg
    );
  }
}
