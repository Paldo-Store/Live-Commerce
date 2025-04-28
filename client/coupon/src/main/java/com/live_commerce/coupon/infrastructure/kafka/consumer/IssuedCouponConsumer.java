package com.live_commerce.coupon.infrastructure.kafka.consumer;

import com.live_commerce.coupon.application.service.IssuedCouponService;
import com.live_commerce.coupon.infrastructure.config.KafkaConfig;
import com.live_commerce.coupon.infrastructure.kafka.dto.FirstJoinCouponMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class IssuedCouponConsumer {

  private final IssuedCouponService issuedCouponService;

  @KafkaListener(
      topics = KafkaConfig.FIRST_COUPON_TOPIC,
      groupId = "coupon-first-join-group",
      containerFactory = "kafkaListenerContainerFactory"
  )
  public void onFirstJoin(FirstJoinCouponMessage msg) {
    issuedCouponService.issueFirstCouponOnSignup(msg.userId());
    log.info("✅ 회원가입 쿠폰이 정상 발급되었습니다.(kafka): userId={}", msg.userId());

  }
}
