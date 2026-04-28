package com.live_commerce.payment.infrastructure.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.live_commerce.payment.application.service.PaymentTxProcessor;
import com.live_commerce.payment.domain.model.Payment;
import com.live_commerce.payment.domain.model.PaymentStatus;
import com.live_commerce.payment.domain.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentExpiredBatchScheduler {

	private final PaymentRepository paymentRepository;
	private final PaymentTxProcessor paymentTxProcessor;

	@Scheduled(fixedDelay = 60_000)
	public void expireOverdue() {
		List<Payment> expired = paymentRepository.findByStatusAndExpiresAtBefore(
			PaymentStatus.PENDING, LocalDateTime.now()
		);

		for (Payment payment : expired) {
			try {
				paymentTxProcessor.fail(payment.getOrderId(), "결제 유효시간 만료(배치)");
			} catch (Exception e) {
				log.error("[Batch] 만료 처리 실패: orderId={}", payment.getOrderId(), e);
			}
		}
	}
}
