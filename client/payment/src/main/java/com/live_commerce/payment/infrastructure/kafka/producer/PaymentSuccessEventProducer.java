package com.live_commerce.payment.infrastructure.kafka.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.live_commerce.payment.application.dto.event.PaymentCompletedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentSuccessEventProducer {

	private final KafkaTemplate<String, String> kafkaTemplate;
	private final ObjectMapper objectMapper;

	private static final String PAYMENT_COMPLETED_TOPIC = "payment-completed";

	public void sendPaymentCompletedEvent(PaymentCompletedEvent event) {
		try {
			String message = objectMapper.writeValueAsString(event);
			kafkaTemplate.send(PAYMENT_COMPLETED_TOPIC, event.orderId().toString(), message);
			log.info("PaymentCompletedEvent 전송 완료: {}", message);
		} catch (Exception e) {
			log.error("PaymentCompletedEvent 직렬화 실패: {}", e.getMessage());
			throw new RuntimeException("이벤트 전송 실패", e);
		}
	}
}
