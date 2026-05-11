package com.live_commerce.payment.application.service;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.live_commerce.payment.domain.model.PaymentMethod;
import com.live_commerce.payment.infrastructure.client.KakaoPayGateway;
import com.live_commerce.payment.domain.model.OutboxStatus;
import com.live_commerce.payment.domain.model.Payment;
import com.live_commerce.payment.domain.model.PaymentOutbox;
import com.live_commerce.payment.domain.model.PaymentStatus;
import com.live_commerce.payment.domain.repository.PaymentOutboxRepository;
import com.live_commerce.payment.domain.repository.PaymentRepository;
import com.live_commerce.payment.infrastructure.client.OrderClient;

@SpringBootTest
@ActiveProfiles("test")
class PaymentTxProcessorTest {

	@Autowired
	private PaymentTxProcessor paymentTxProcessor;
	@Autowired
	private PaymentRepository paymentRepository;
	@Autowired
	private PaymentOutboxRepository paymentOutboxRepository;

	@MockitoBean
	private KakaoPayGateway kakaoPayGateway;
	@MockitoBean
	private OrderClient orderClient;

	private UUID userId;
	private UUID orderId;

	@BeforeEach
	void setup() {
		paymentOutboxRepository.deleteAll();
		paymentRepository.deleteAll();
		userId = UUID.randomUUID();
		orderId = UUID.randomUUID();
	}

	@DisplayName("complete - PAYMENT_COMPLETED Outbox 레코드 저장 및 결제 상태 변경")
	@Test
	void complete_savesOutboxAndChangesStatus() {
		paymentRepository.save(Payment.of(userId, orderId, BigDecimal.valueOf(10000), PaymentMethod.KAKAO));

		paymentTxProcessor.complete(orderId, "test-payment-key");

		Payment updated = paymentRepository.findByOrderId(orderId).orElseThrow();
		assertEquals(PaymentStatus.COMPLETED, updated.getStatus());

		List<PaymentOutbox> records = paymentOutboxRepository.findAll();
		assertEquals(1, records.size());
		PaymentOutbox outbox = records.get(0);
		assertEquals("PAYMENT_COMPLETED", outbox.getEventType());
		assertEquals(OutboxStatus.PENDING, outbox.getStatus());
		assertTrue(outbox.getPayload().contains(orderId.toString()));
		assertTrue(outbox.getPayload().contains("10000"));
	}

	@DisplayName("fail - PAYMENT_FAILED Outbox 레코드 저장 및 결제 상태 변경")
	@Test
	void fail_savesOutboxAndChangesStatus() {
		paymentRepository.save(Payment.of(userId, orderId, BigDecimal.valueOf(5000), PaymentMethod.KAKAO));

		paymentTxProcessor.fail(orderId, "카드 한도 초과");

		Payment updated = paymentRepository.findByOrderId(orderId).orElseThrow();
		assertEquals(PaymentStatus.FAILED, updated.getStatus());

		List<PaymentOutbox> records = paymentOutboxRepository.findAll();
		assertEquals(1, records.size());
		PaymentOutbox outbox = records.get(0);
		assertEquals("PAYMENT_FAILED", outbox.getEventType());
		assertTrue(outbox.getPayload().contains("카드 한도 초과"));
		assertTrue(outbox.getPayload().contains(orderId.toString()));
	}

	@DisplayName("refund - 결제 상태 REFUND 변경, Outbox 저장 없음")
	@Test
	void refund_changesStatusOnly() {
		Payment payment = Payment.of(userId, orderId, BigDecimal.valueOf(8000), PaymentMethod.KAKAO);
		payment.updateStatus(PaymentStatus.COMPLETED);
		paymentRepository.save(payment);

		paymentTxProcessor.refund(orderId);

		assertEquals(PaymentStatus.REFUND, paymentRepository.findByOrderId(orderId).orElseThrow().getStatus());
		assertEquals(0, paymentOutboxRepository.count());
	}

	@DisplayName("cancel - 결제 상태 CANCELED 변경, Outbox 저장 없음")
	@Test
	void cancel_changesStatusOnly() {
		paymentRepository.save(Payment.of(userId, orderId, BigDecimal.valueOf(3000), PaymentMethod.KAKAO));

		paymentTxProcessor.cancel(orderId);

		assertEquals(PaymentStatus.CANCELED, paymentRepository.findByOrderId(orderId).orElseThrow().getStatus());
		assertEquals(0, paymentOutboxRepository.count());
	}
}
