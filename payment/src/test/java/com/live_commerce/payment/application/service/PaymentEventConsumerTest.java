package com.live_commerce.payment.application.service;

import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.live_commerce.payment.infrastructure.kafka.consumer.PaymentEventConsumer;
import com.live_commerce.payment.infrastructure.kafka.event.OrderFailedEvent;

public class PaymentEventConsumerTest {

	private PaymentServiceV2 paymentServiceV2;
	private PaymentEventConsumer consumer;

	@BeforeEach
	void setUp() {
		paymentServiceV2 = mock(PaymentServiceV2.class);
		consumer = new PaymentEventConsumer(paymentServiceV2);
	}

	@DisplayName("OrderFailedEvent 수신 시 결제 보상 로직을 호출한다")
	@Test
	void shouldCallCompensateRefundOnOrderFailedEvent() throws Exception {
		// given
		UUID orderId = UUID.randomUUID();
		String reason = "재고 부족";
		OrderFailedEvent event = new OrderFailedEvent(orderId, reason);

		// when
		consumer.listenOrderFailed(event);

		// then
		verify(paymentServiceV2).compensateRefundByOrderId(orderId, reason);
	}
}