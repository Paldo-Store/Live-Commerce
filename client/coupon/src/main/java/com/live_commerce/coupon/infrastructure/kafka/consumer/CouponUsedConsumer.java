package com.live_commerce.coupon.infrastructure.kafka.consumer;

import com.live_commerce.coupon.infrastructure.config.KafkaConfig;
import com.live_commerce.coupon.infrastructure.kafka.dto.CouponUsedMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CouponUsedConsumer {

  @KafkaListener(
      topics = KafkaConfig.COUPON_USED_TOPIC,
      groupId = "coupon-used-group",
      containerFactory = "couponUsedListenerContainerFactory")
  public void onCouponUsed(CouponUsedMessage msg) {
    log.info("✅ 쿠폰 사용 성공 이벤트 수신(kafka): couponId={}, userId={}", msg.couponId(), msg.userId());
  }
}
