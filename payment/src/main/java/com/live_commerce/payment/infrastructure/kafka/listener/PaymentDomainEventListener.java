package com.live_commerce.payment.infrastructure.kafka.listener;

import java.util.concurrent.TimeUnit;

import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.live_commerce.payment.domain.event.PaymentReadyDomainEvent;
import com.live_commerce.payment.infrastructure.redis.PaymentRedisKeys;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentDomainEventListener {

	private final RedissonClient redissonClient;
	private final RetryTemplate retryTemplate;

	@Value("${payment.expire-minutes:10}")
	private int paymentExpireMinutes;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void onPaymentReady(PaymentReadyDomainEvent event) {
		try {
			retryTemplate.execute(ctx -> {
				redissonClient.getBucket(PaymentRedisKeys.EXPIRE_KEY_PREFIX + event.orderId())
					.set(event.paymentId().toString(), paymentExpireMinutes, TimeUnit.MINUTES);
				return null;
			});
		} catch (Exception e) {
			log.error("[Payment] Redis 만료 key 설정 실패 - PENDING 상태 보정 불가: orderId={}", event.orderId(), e);
		}
	}
}
