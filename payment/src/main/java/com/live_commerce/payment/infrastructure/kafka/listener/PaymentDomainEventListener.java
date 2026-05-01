package com.live_commerce.payment.infrastructure.kafka.listener;

import java.util.concurrent.TimeUnit;

import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.live_commerce.payment.infrastructure.redis.PaymentRedisKeys;
import com.live_commerce.payment.domain.event.PaymentCompletedDomainEvent;
import com.live_commerce.payment.domain.event.PaymentFailedDomainEvent;
import com.live_commerce.payment.domain.event.PaymentReadyDomainEvent;
import com.live_commerce.payment.infrastructure.kafka.event.PaymentCompletedEvent;
import com.live_commerce.payment.infrastructure.kafka.event.PaymentFailedEvent;
import com.live_commerce.payment.infrastructure.kafka.producer.PaymentEventProducer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentDomainEventListener {

	private final PaymentEventProducer paymentEventProducer;
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

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void onPaymentCompleted(PaymentCompletedDomainEvent event) {
		paymentEventProducer.sendPaymentCompleted(
			new PaymentCompletedEvent(event.orderId(), "결제 완료", event.amount())
		);
	}

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void onPaymentFailed(PaymentFailedDomainEvent event) {
		paymentEventProducer.sendPaymentFailed(
			new PaymentFailedEvent(event.orderId(), event.reason())
		);
	}
}
