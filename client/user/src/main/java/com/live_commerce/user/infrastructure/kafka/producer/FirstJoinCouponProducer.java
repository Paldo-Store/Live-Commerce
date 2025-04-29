package com.live_commerce.user.infrastructure.kafka.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.live_commerce.user.infrastructure.kafka.event.FirstJoinCouponEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class FirstJoinCouponProducer {

	private final KafkaTemplate<String, FirstJoinCouponEvent> kafkaTemplate;
	private static final String FIRST_COUPON_TOPIC = "first-coupon-topic";

	public void send(FirstJoinCouponEvent event) {
		kafkaTemplate.send(FIRST_COUPON_TOPIC, event.userId().toString(), event);
		log.info("[Kafka] 첫 가입 쿠폰 이벤트 전송 완료: {}", event);
	}
}
