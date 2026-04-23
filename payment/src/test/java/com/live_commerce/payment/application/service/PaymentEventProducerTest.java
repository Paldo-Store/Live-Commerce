package com.live_commerce.payment.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;

import com.live_commerce.payment.infrastructure.kafka.event.PaymentCompletedEvent;
import com.live_commerce.payment.infrastructure.kafka.producer.PaymentEventProducer;

public class PaymentEventProducerTest {

	private KafkaTemplate<String, Object> kafkaTemplate = mock(KafkaTemplate.class);
	private PaymentEventProducer producer;

	@BeforeEach
	void setUp() {
		producer = new PaymentEventProducer(kafkaTemplate);
	}

	@DisplayName("결제 완료 이벤트를 JSON으로 직렬화하고 발행한다")
	@Test
	void shouldSerializeAndPublishPaymentCompletedEvent() throws Exception {
		// given
		UUID orderId = UUID.randomUUID();
		PaymentCompletedEvent event =
			new PaymentCompletedEvent(orderId, "결제완료", BigDecimal.valueOf(990000, 2));

		// when
		producer.sendPaymentCompleted(event);

		// then
		ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);

		verify(kafkaTemplate).send(topicCaptor.capture(), keyCaptor.capture(), valueCaptor.capture());

		assertThat(topicCaptor.getValue()).isEqualTo("payment-completed");
		assertThat(keyCaptor.getValue()).isEqualTo(orderId.toString());
		assertThat(valueCaptor.getValue())
			.contains("\"orderId\":\"" + orderId + "\"")
			.contains("\"message\":\"결제완료\"")
			.contains("\"finalPaidPrice\":9900.00"); // 소수점 2자리
	}
}
