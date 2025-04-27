package com.live_commerce.payment.infrastructure.kafka.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.live_commerce.payment.application.dto.event.PaymentCompletedEvent;
import com.live_commerce.payment.infrastructure.kafka.dto.PaymentFailedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventProducer {

	private final KafkaTemplate<String, String> kafkaTemplate;
	private final ObjectMapper objectMapper = new ObjectMapper();

	private static final String COMPLETED_TOPIC = "payment-completed";
	private static final String FAILED_TOPIC    = "payment-failed";

	public void sendPaymentCompleted(PaymentCompletedEvent event) {
		send(COMPLETED_TOPIC, event.orderId().toString(), event);
	}

	public void sendPaymentFailed(PaymentFailedEvent event) {
		send(FAILED_TOPIC, event.orderId().toString(), event);
	}

	// 공통 전송 로직
	private void send(String topic, String key, Object payload) {
		try {
			String json = objectMapper.writeValueAsString(payload);
			kafkaTemplate.send(topic, key, json);
			log.info("[Kafka] {} → {}", topic, json);
		} catch (JsonProcessingException e) {
			log.error("[Kafka] 직렬화 오류: {}", e.getMessage(), e);
		}
	}
}