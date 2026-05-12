package com.live_commerce.payment.infrastructure.scheduler;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.live_commerce.payment.domain.model.PaymentOutbox;
import com.live_commerce.payment.domain.repository.PaymentOutboxRepository;
import com.live_commerce.payment.infrastructure.kafka.event.PaymentCanceledEvent;
import com.live_commerce.payment.infrastructure.kafka.event.PaymentCompletedEvent;
import com.live_commerce.payment.infrastructure.kafka.event.PaymentFailedEvent;
import com.live_commerce.payment.infrastructure.kafka.event.PaymentRefundedEvent;
import com.live_commerce.payment.infrastructure.kafka.producer.PaymentEventProducer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxRecordProcessor {

	private static final int MAX_RETRY = 3;

	private final PaymentOutboxRepository outboxRepository;
	private final PaymentEventProducer paymentEventProducer;
	private final ObjectMapper objectMapper;
	private final TransactionTemplate transactionTemplate; // propagation = REQUIRED (default)

	// Kafka 호출을 트랜잭션 밖에서 수행하고, DB 업데이트만 짧은 트랜잭션으로 처리
	public void process(PaymentOutbox outbox) {
		try {
			publish(outbox);
			outbox.markPublished();
		} catch (JsonProcessingException | IllegalArgumentException e) {
			// payload 파싱 실패 / 알 수 없는 eventType → 재시도해도 동일 결과, 즉시 FAILED
			outbox.markFailed();
			log.error("[Outbox] 복구 불가 오류 - 수동 처리 필요: id={}, orderId={}", outbox.getId(), outbox.getOrderId(), e);
		} catch (Exception e) {
			// Kafka 전송 실패 등 일시적 오류 → 재시도
			outbox.incrementRetry();
			if (outbox.getRetryCount() >= MAX_RETRY) {
				outbox.markFailed();
				log.error("[Outbox] 최대 재시도 초과 - 수동 처리 필요: id={}, orderId={}", outbox.getId(), outbox.getOrderId(), e);
			} else {
				log.warn("[Outbox] 발행 실패 재시도 예정: id={}, retry={}", outbox.getId(), outbox.getRetryCount(), e);
			}
		}
		try {
			transactionTemplate.executeWithoutResult(tx ->
				outboxRepository.updateRecord(outbox.getId(), outbox.getStatus(), outbox.getPublishedAt(), outbox.getRetryCount())
			);
		} catch (Exception e) {
			log.error("[Outbox] DB 업데이트 실패: id={}, orderId={}", outbox.getId(), outbox.getOrderId(), e);
		}
	}

	private void publish(PaymentOutbox outbox) throws Exception {
		Map<String, Object> map = objectMapper.readValue(
			outbox.getPayload(), new TypeReference<Map<String, Object>>() {}
		);
		UUID orderId = UUID.fromString((String) map.get("orderId"));

		switch (outbox.getEventType()) {
			case "PAYMENT_COMPLETED" -> {
				BigDecimal amount = new BigDecimal((String) map.get("amount"));
				paymentEventProducer.sendPaymentCompleted(new PaymentCompletedEvent(orderId, "결제 완료", amount));
			}
			case "PAYMENT_FAILED" -> {
				String message = (String) map.get("message");
				paymentEventProducer.sendPaymentFailed(new PaymentFailedEvent(orderId, message));
			}
			case "PAYMENT_REFUNDED" -> {
				BigDecimal amount = new BigDecimal((String) map.get("amount"));
				paymentEventProducer.sendPaymentRefunded(new PaymentRefundedEvent(orderId, amount));
			}
			case "PAYMENT_CANCELED" ->
				paymentEventProducer.sendPaymentCanceled(new PaymentCanceledEvent(orderId));
			case "PAYMENT_RECOVERY_NEEDED" -> {
				String message = (String) map.get("message");
				log.error("[Outbox] 수동 복구 필요 — PG사 취소 미완료: orderId={}, reason={}", orderId, message);
			}
			default -> throw new IllegalArgumentException("알 수 없는 eventType: " + outbox.getEventType());
		}
	}
}
