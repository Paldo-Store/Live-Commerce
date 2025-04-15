package com.live_commerce.payment.application.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.live_commerce.payment.domain.model.Payment;
import com.live_commerce.payment.domain.model.PaymentStatus;
import com.live_commerce.payment.domain.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentExpireScheduler {

	private final PaymentRepository paymentRepository;

	@Scheduled(fixedDelay = 1000 * 60 * 10) // 10분에 한번씩 실행
	@Transactional
	public void expirePendingPayments() {
		LocalDateTime threshold = LocalDateTime.now().minusMinutes(10);
		List<Payment> expiredPayments = paymentRepository.findAllByStatusAndCreatedAtBefore(PaymentStatus.PENDING, threshold);
		expiredPayments.forEach(payment -> {
			payment.updateStatus(PaymentStatus.CANCELED);
			log.info("🔁 자동 취소 처리: paymentId={}, orderId={}", payment.getId(), payment.getOrderId());
		});
	}
}

