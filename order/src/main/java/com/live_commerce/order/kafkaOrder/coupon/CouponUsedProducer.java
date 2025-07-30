package com.live_commerce.order.kafkaOrder.coupon;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponUsedProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TOPIC = "coupon-used";

    public void sendCouponUsedEvent(CouponUsedEvent event) {
        kafkaTemplate.send(TOPIC, event.userId().toString(), event);
        log.info("✅ 쿠폰 사용 이벤트 전송 성공: couponId={}, userId={}", event.couponId(), event.userId());
    }
}