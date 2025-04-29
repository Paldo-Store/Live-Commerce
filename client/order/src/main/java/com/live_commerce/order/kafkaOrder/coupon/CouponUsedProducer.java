package com.live_commerce.order.kafkaOrder.coupon;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponUsedProducer {

    private final KafkaTemplate<String, CouponUsedMessage> kafkaTemplate;

    public void sendCouponUsedEvent(CouponUsedMessage event) {
        kafkaTemplate.send("coupon-used-topic", event.userId().toString(), event);
        log.info("✅ 쿠폰 사용 이벤트 전송 성공: couponId={}, userId={}", event.couponId(), event.userId());
    }
}