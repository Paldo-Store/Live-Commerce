package com.live_commerce.order.infrastructure.kafka.consumor;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.live_commerce.order.infrastructure.kafka.dto.PaymentCompletedEvent;
import com.live_commerce.order.infrastructure.kafka.dto.PaymentFailedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

	private final ObjectMapper objectMapper;

	// 결제 완료 이벤트 수신 (테스트용으로 간단히 구현한 것으로 참고해서 실제 로직 작성하시면 됩니다.)
	@KafkaListener(topics = "payment-completed", groupId = "order-group")
	public void listenPaymentCompleted(String message) {
		try {
			PaymentCompletedEvent event = objectMapper.readValue(message, PaymentCompletedEvent.class);
			log.info("[Order] 결제 완료 이벤트 수신 - orderId: {}, message: {}, finalPaidPrice: {}",
				event.orderId(), event.message(), event.finalPaidPrice());
		} catch (Exception e) {
			log.error("[Order] 결제 완료 이벤트 파싱 실패: {}", e.getMessage(), e);
		}
	}

	// 결제 실패 이벤트 수신 (테스트용으로 간단히 구현한 것으로 참고해서 실제 로직 작성하시면 됩니다.)
	@KafkaListener(topics = "payment-failed", groupId = "order-group")
	public void listenPaymentFailed(String message) {
		try {
			PaymentFailedEvent event = objectMapper.readValue(message, PaymentFailedEvent.class);
			log.info("[Order] 결제 실패 이벤트 수신 - orderId: {}, message: {}",
				event.orderId(), event.message());
		} catch (Exception e) {
			log.error("[Order] 결제 실패 이벤트 파싱 실패: {}", e.getMessage(), e);
		}
	}
}
