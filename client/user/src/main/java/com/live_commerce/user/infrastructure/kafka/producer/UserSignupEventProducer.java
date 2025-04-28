package com.live_commerce.user.infrastructure.kafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.live_commerce.user.infrastructure.kafka.dto.FirstJoinCouponMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserSignupEventProducer {

	private final KafkaTemplate<String, String> kafkaTemplate;
	private final ObjectMapper objectMapper = new ObjectMapper();

	private static final String FIRST_COUPON_TOPIC = "first-coupon-topic";

	public void sendFirstJoinCouponEvent(FirstJoinCouponMessage event) {
		try {
			String json = objectMapper.writeValueAsString(event);
			kafkaTemplate.send(FIRST_COUPON_TOPIC, event.userId().toString(), json);
			log.info("[Kafka] 회원가입 쿠폰 이벤트 전송 완료: {}", json);
		} catch (JsonProcessingException e) {
			log.error("[Kafka] 직렬화 실패: {}", e.getMessage(), e);
		}
	}
}
