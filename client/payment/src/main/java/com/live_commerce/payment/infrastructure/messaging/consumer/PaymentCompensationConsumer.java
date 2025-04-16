package com.live_commerce.payment.infrastructure.messaging.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.live_commerce.payment.application.dto.event.OrderCancelEvent;
import com.live_commerce.payment.application.exception.CustomException;
import com.live_commerce.payment.application.exception.PaymentExceptionCode;
import com.live_commerce.payment.application.port.KakaoPayClient;
import com.live_commerce.payment.domain.model.Payment;
import com.live_commerce.payment.domain.model.PaymentStatus;
import com.live_commerce.payment.domain.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentCompensationConsumer {

	private final ObjectMapper objectMapper;
	private final PaymentRepository paymentRepository;
	private final KakaoPayClient kakaoPayClient;

	@KafkaListener(topics = "order-cancel-requested", groupId = "payment-service")
	public void handleOrderCancel(String message) {
		try {
			OrderCancelEvent event = objectMapper.readValue(message, OrderCancelEvent.class);

			Payment payment = paymentRepository.findByOrderId(event.orderId())
				.orElseThrow(() -> new CustomException(PaymentExceptionCode.NOT_FOUND));

			if (payment.getStatus() == PaymentStatus.COMPLETED) {
				kakaoPayClient.requestKakaoPayCancel(payment.getTid(), payment.getAmount());
				payment.updateStatus(PaymentStatus.REFUND);
				log.info("보상 트랜잭션: 결제 환불 완료 - orderId: {}", event.orderId());
			} else if (payment.getStatus() == PaymentStatus.PENDING) {
				payment.updateStatus(PaymentStatus.CANCELED);
				log.info("보상 트랜잭션: 결제 취소 완료 - orderId: {}", event.orderId());
			}
		} catch (Exception e) {
			log.error("보상 트랜잭션 처리 실패", e);
		}
	}
}

