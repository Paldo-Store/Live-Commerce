package com.live_commerce.order.kafkaOrder.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.live_commerce.order.kafkaOrder.service.OrderServiceKafka;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

	private final OrderServiceKafka orderServiceKafka;
	private final ObjectMapper objectMapper;

	// 결제 완료 이벤트 수신 (테스트용으로 간단히 구현한 것으로 참고해서 실제 로직 작성하시면 됩니다.)
	@KafkaListener(topics = "payment-completed", groupId = "order-group")
	public void listenPaymentCompleted(String message) {
		try {
			PaymentCompletedEvent event = objectMapper.readValue(message, PaymentCompletedEvent.class);
			log.info("[Order] 결제 완료 이벤트 수신 - orderId: {}, message: {}, finalPaidPrice: {}",
					event.orderId(), event.message(), event.finalPaidPrice());

			orderServiceKafka.updatePaymentSuccessKafka(event);
		} catch (Exception e) {
			log.error("[Order] 결제 완료 이벤트 파싱 실패: {}", e.getMessage(), e);
		}
	}

	//TODO 보상 트랜잭션
	// 결제 실패 이벤트 수신 (테스트용으로 간단히 구현한 것으로 참고해서 실제 로직 작성하시면 됩니다.)
//	@KafkaListener(
//			topics = "payment-failed",
//			groupId = "order-group",
//			containerFactory = "kafkaListenerContainerFactory"
//	)
//	public void listenPaymentFailed(PaymentFailedEvent event) {
//		log.info("[Order] 결제 실패 이벤트 수신 - orderId: {}, message: {}",
//				event.orderId(), event.message());
//
//		// ✅ OrderServiceKafka로 넘겨서 비즈니스 로직 처리
//		orderServiceKafka.handlePaymentFailed(event);
//	}
}