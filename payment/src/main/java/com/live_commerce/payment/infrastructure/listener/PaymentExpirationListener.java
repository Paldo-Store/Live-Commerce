package com.live_commerce.payment.infrastructure.listener;

import java.util.UUID;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import com.live_commerce.payment.application.service.PaymentTxProcessor;
import com.live_commerce.payment.domain.model.PaymentStatus;
import com.live_commerce.payment.domain.repository.PaymentRepository;
import com.live_commerce.payment.infrastructure.redis.PaymentRedisKeys;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentExpirationListener implements MessageListener {

	private final PaymentRepository paymentRepository;
	private final PaymentTxProcessor paymentTxProcessor;

	@Override
	public void onMessage(Message message, byte[] pattern) {
		String expiredKey = new String(message.getBody());

		if (!expiredKey.startsWith(PaymentRedisKeys.EXPIRE_KEY_PREFIX)) {
			return;
		}

		try {
			UUID orderId = UUID.fromString(expiredKey.replace(PaymentRedisKeys.EXPIRE_KEY_PREFIX, ""));

			paymentRepository.findByOrderIdAndStatus(orderId, PaymentStatus.PENDING)
				.ifPresent(payment -> {
					try {
						paymentTxProcessor.fail(orderId, "결제 유효시간 만료");
					} catch (IllegalStateException e) {
						log.warn("[Expiration] 상태 전이 불가 - 이미 처리됨: orderId={}", orderId);
					}
				});
		} catch (Exception e) {
			log.error("[Expiration] Redis TTL 처리 중 예외 발생: key={}", expiredKey, e);
		}
	}
}
