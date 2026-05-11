package com.live_commerce.payment.infrastructure.scheduler;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.live_commerce.payment.domain.model.OutboxStatus;
import com.live_commerce.payment.domain.model.PaymentOutbox;
import com.live_commerce.payment.domain.repository.PaymentOutboxRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxRelayScheduler {

	private static final String RELAY_LOCK_KEY = "payment:outbox:relay:lock";

	private final PaymentOutboxRepository outboxRepository;
	private final OutboxRecordProcessor outboxRecordProcessor;
	private final RedissonClient redissonClient;

	@Scheduled(fixedDelay = 3000)
	public void relay() {
		RLock lock = redissonClient.getLock(RELAY_LOCK_KEY);
		try {
			if (!lock.tryLock(0, 2, TimeUnit.MINUTES)) {
				return;
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			log.error("Outbox relay 인터럽트 발생");
			return;
		}
		try {
			List<PaymentOutbox> pending = outboxRepository.findTop50ByStatusOrderByCreatedAt(OutboxStatus.PENDING);
			for (PaymentOutbox outbox : pending) {
				outboxRecordProcessor.process(outbox);
			}
		} finally {
			if (lock.isHeldByCurrentThread()) {
				lock.unlock();
			}
		}
	}
}
