package com.live_commerce.payment.infrastructure.messaging.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.live_commerce.payment.application.dto.event.PaymentCompletedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventConsumer {

	private final ObjectMapper objectMapper;

	// 테스트용
	@KafkaListener(topics = "payment-completed", groupId = "order-service")
	public void listenPaymentCompleted(ConsumerRecord<String, String> record) {
		try {
			String message = record.value();
			log.info("수신된 PaymentCompletedEvent: {}", message);

			PaymentCompletedEvent event = objectMapper.readValue(message, PaymentCompletedEvent.class);
		} catch (Exception e) {
			log.error("PaymentCompletedEvent 처리 실패: {}", e.getMessage());
		}
	}
}