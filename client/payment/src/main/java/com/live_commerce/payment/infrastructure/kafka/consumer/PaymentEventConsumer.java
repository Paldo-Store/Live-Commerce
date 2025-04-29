package com.live_commerce.payment.infrastructure.kafka.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.live_commerce.payment.application.service.PaymentServiceV2;
import com.live_commerce.payment.infrastructure.kafka.event.OrderFailedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

	private final PaymentServiceV2 paymentServiceV2;

	@KafkaListener(topics = "order-failed", groupId = "payment-compensation-group")
	public void listenOrderFailed(OrderFailedEvent event) {
		try {
			log.info("[Kafka] 주문 실패 이벤트 수신: orderId = {}, message = {}", event.orderId(), event.message());
			paymentServiceV2.compensateRefundByOrderId(event.orderId(), event.message());
		} catch (Exception e) {
			log.error("[Kafka] 주문 실패 이벤트 처리 실패: {}", e.getMessage(), e);
		}
	}
}

