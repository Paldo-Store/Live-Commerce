package com.live_commerce.payment.infrastructure.scheduler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.live_commerce.payment.application.port.KakaoPayClient;
import com.live_commerce.payment.domain.model.OutboxStatus;
import com.live_commerce.payment.domain.model.PaymentOutbox;
import com.live_commerce.payment.domain.repository.PaymentOutboxRepository;
import com.live_commerce.payment.infrastructure.client.OrderClient;
import com.live_commerce.payment.infrastructure.kafka.producer.PaymentEventProducer;

@SpringBootTest
@ActiveProfiles("test")
class OutboxRecordProcessorTest {

	@Autowired
	private OutboxRecordProcessor outboxRecordProcessor;
	@Autowired
	private PaymentOutboxRepository paymentOutboxRepository;

	@MockitoBean
	private PaymentEventProducer paymentEventProducer;
	@MockitoBean
	private KakaoPayClient kakaoPayClient;
	@MockitoBean
	private OrderClient orderClient;

	private UUID orderId;

	@BeforeEach
	void setup() {
		paymentOutboxRepository.deleteAll();
		orderId = UUID.randomUUID();
	}

	@DisplayName("PAYMENT_COMPLETED - Kafka 발행 성공 시 PUBLISHED 상태로 변경")
	@Test
	void process_completed_marksPublished() {
		PaymentOutbox outbox = saveOutbox("PAYMENT_COMPLETED",
			"""
			{"orderId":"%s","amount":"10000"}
			""".formatted(orderId).trim());

		outboxRecordProcessor.process(outbox);

		verify(paymentEventProducer).sendPaymentCompleted(any());
		PaymentOutbox result = paymentOutboxRepository.findById(outbox.getId()).orElseThrow();
		assertEquals(OutboxStatus.PUBLISHED, result.getStatus());
		assertNotNull(result.getPublishedAt());
		assertEquals(0, result.getRetryCount());
	}

	@DisplayName("PAYMENT_FAILED - Kafka 발행 성공 시 PUBLISHED 상태로 변경")
	@Test
	void process_failed_marksPublished() {
		PaymentOutbox outbox = saveOutbox("PAYMENT_FAILED",
			"""
			{"orderId":"%s","reason":"결제 실패"}
			""".formatted(orderId).trim());

		outboxRecordProcessor.process(outbox);

		verify(paymentEventProducer).sendPaymentFailed(any());
		assertEquals(OutboxStatus.PUBLISHED,
			paymentOutboxRepository.findById(outbox.getId()).orElseThrow().getStatus());
	}

	@DisplayName("Kafka 발행 실패 - retryCount 증가, PENDING 유지")
	@Test
	void process_kafkaFail_incrementsRetry() {
		PaymentOutbox outbox = saveOutbox("PAYMENT_COMPLETED",
			"""
			{"orderId":"%s","amount":"10000"}
			""".formatted(orderId).trim());
		doThrow(new RuntimeException("Kafka 오류")).when(paymentEventProducer).sendPaymentCompleted(any());

		outboxRecordProcessor.process(outbox);

		PaymentOutbox result = paymentOutboxRepository.findById(outbox.getId()).orElseThrow();
		assertEquals(OutboxStatus.PENDING, result.getStatus());
		assertEquals(1, result.getRetryCount());
	}

	@DisplayName("MAX_RETRY 초과 - FAILED 상태로 변경")
	@Test
	void process_maxRetryReached_marksFailed() {
		PaymentOutbox outbox = PaymentOutbox.of(orderId, "PAYMENT_COMPLETED",
			"""
			{"orderId":"%s","amount":"10000"}
			""".formatted(orderId).trim());
		outbox.incrementRetry(); // retryCount = 1
		outbox.incrementRetry(); // retryCount = 2
		paymentOutboxRepository.save(outbox);
		doThrow(new RuntimeException("Kafka 오류")).when(paymentEventProducer).sendPaymentCompleted(any());

		outboxRecordProcessor.process(outbox); // incrementRetry → 3 >= MAX_RETRY(3)

		PaymentOutbox result = paymentOutboxRepository.findById(outbox.getId()).orElseThrow();
		assertEquals(OutboxStatus.FAILED, result.getStatus());
		assertEquals(3, result.getRetryCount());
	}

	@DisplayName("알 수 없는 eventType - 재시도 없이 즉시 FAILED")
	@Test
	void process_unknownEventType_immediatelyFailed() {
		PaymentOutbox outbox = saveOutbox("UNKNOWN_TYPE",
			"""
			{"orderId":"%s","amount":"10000"}
			""".formatted(orderId).trim());

		outboxRecordProcessor.process(outbox);

		PaymentOutbox result = paymentOutboxRepository.findById(outbox.getId()).orElseThrow();
		assertEquals(OutboxStatus.FAILED, result.getStatus());
		assertEquals(0, result.getRetryCount()); // retry 없이 바로 FAILED
	}

	@DisplayName("payload JSON 파싱 실패 - 재시도 없이 즉시 FAILED")
	@Test
	void process_invalidPayload_immediatelyFailed() {
		PaymentOutbox outbox = saveOutbox("PAYMENT_COMPLETED", "not-a-json");

		outboxRecordProcessor.process(outbox);

		PaymentOutbox result = paymentOutboxRepository.findById(outbox.getId()).orElseThrow();
		assertEquals(OutboxStatus.FAILED, result.getStatus());
		assertEquals(0, result.getRetryCount());
		verify(paymentEventProducer, never()).sendPaymentCompleted(any());
	}

	private PaymentOutbox saveOutbox(String eventType, String payload) {
		return paymentOutboxRepository.save(PaymentOutbox.of(orderId, eventType, payload));
	}
}
