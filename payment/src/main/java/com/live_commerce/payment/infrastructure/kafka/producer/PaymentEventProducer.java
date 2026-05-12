package com.live_commerce.payment.infrastructure.kafka.producer;

import java.util.concurrent.ExecutionException;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.live_commerce.payment.infrastructure.kafka.event.PaymentCanceledEvent;
import com.live_commerce.payment.infrastructure.kafka.event.PaymentCompletedEvent;
import com.live_commerce.payment.infrastructure.kafka.event.PaymentFailedEvent;
import com.live_commerce.payment.infrastructure.kafka.event.PaymentRefundedEvent;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PaymentEventProducer {

	private final KafkaTemplate<String, Object> kafkaTemplate;

	private static final String COMPLETED_TOPIC = "payment-completed";
	private static final String FAILED_TOPIC    = "payment-failed";
	private static final String REFUNDED_TOPIC  = "payment-refunded";
	private static final String CANCELED_TOPIC  = "payment-canceled";

	public void sendPaymentCompleted(PaymentCompletedEvent event) {
		sendSync(COMPLETED_TOPIC, event.orderId().toString(), event);
	}

	public void sendPaymentFailed(PaymentFailedEvent event) {
		sendSync(FAILED_TOPIC, event.orderId().toString(), event);
	}

	public void sendPaymentRefunded(PaymentRefundedEvent event) {
		sendSync(REFUNDED_TOPIC, event.orderId().toString(), event);
	}

	public void sendPaymentCanceled(PaymentCanceledEvent event) {
		sendSync(CANCELED_TOPIC, event.orderId().toString(), event);
	}

	private void sendSync(String topic, String key, Object payload) {
		try {
			kafkaTemplate.send(topic, key, payload).get();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException("Kafka 전송 인터럽트: topic=" + topic, e);
		} catch (ExecutionException e) {
			throw new RuntimeException("Kafka 전송 실패: topic=" + topic, e.getCause());
		}
	}
}
