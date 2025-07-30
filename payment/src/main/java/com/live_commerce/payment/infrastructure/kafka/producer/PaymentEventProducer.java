package com.live_commerce.payment.infrastructure.kafka.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.live_commerce.payment.infrastructure.kafka.event.PaymentCompletedEvent;
import com.live_commerce.payment.infrastructure.kafka.event.PaymentFailedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventProducer {

	private final KafkaTemplate<String, Object> kafkaTemplate;

	private static final String COMPLETED_TOPIC = "payment-completed";
	private static final String FAILED_TOPIC    = "payment-failed";

	public void sendPaymentCompleted(PaymentCompletedEvent event) {
		send(COMPLETED_TOPIC, event.orderId().toString(), event);
	}

	public void sendPaymentFailed(PaymentFailedEvent event) {
		send(FAILED_TOPIC, event.orderId().toString(), event);
	}

	private void send(String topic, String key, Object payload) {
		kafkaTemplate.send(topic, key, payload);
		log.info("[Kafka] 전송 완료 - topic: {}, key: {}, payload: {}", topic, key, payload);
	}
}
