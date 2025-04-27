package com.live_commerce.payment.application.service;

import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.live_commerce.payment.infrastructure.kafka.consumer.PaymentEventConsumer;
import com.live_commerce.payment.infrastructure.kafka.dto.OrderFailedEvent;

public class PaymentEventConsumerTest {

	private ObjectMapper objectMapper;
	private PaymentServiceV2 paymentServiceV2;
	private PaymentEventConsumer consumer;

	@BeforeEach
	void setUp() {
		objectMapper = new ObjectMapper();
		paymentServiceV2 = mock(PaymentServiceV2.class);
		consumer = new PaymentEventConsumer(objectMapper, paymentServiceV2);
	}

	@DisplayName("OrderFailedEvent 수신 시 결제 보상 로직을 호출한다")
	@Test
	void shouldCallCompensateRefundOnOrderFailedEvent() throws Exception {
		// given
		UUID orderId = UUID.randomUUID();
		String reason = "재고 부족";
		OrderFailedEvent event = new OrderFailedEvent(orderId, reason);
		String json = objectMapper.writeValueAsString(event);

		// when
		consumer.listenOrderFailed(json);

		// then
		verify(paymentServiceV2).compensateRefundByOrderId(orderId, reason);
	}
}