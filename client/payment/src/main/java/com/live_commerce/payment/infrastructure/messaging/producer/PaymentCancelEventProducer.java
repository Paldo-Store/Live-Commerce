package com.live_commerce.payment.infrastructure.messaging.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.live_commerce.payment.application.dto.event.PaymentCancelEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentCancelEventProducer {

	private final KafkaTemplate<String, String> kafkaTemplate;
	private final ObjectMapper objectMapper;

	private static final String PAYMENT_CANCELED_TOPIC = "payment-canceled";

	public void sendPaymentCancelEvent(PaymentCancelEvent event) {
		try {
			String message = objectMapper.writeValueAsString(event);
			kafkaTemplate.send(PAYMENT_CANCELED_TOPIC, event.orderId().toString(), message);
			log.info("PaymentCancelEvent 전송 완료: {}", message);
		} catch (Exception e) {
			log.error("PaymentCancelEvent 직렬화 또는 전송 실패", e);
			throw new RuntimeException("PaymentCancelEvent Kafka 전송 실패", e);
		}
	}
}
