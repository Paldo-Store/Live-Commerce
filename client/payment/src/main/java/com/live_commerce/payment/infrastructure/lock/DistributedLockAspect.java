package com.live_commerce.payment.infrastructure.lock;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import com.live_commerce.payment.application.exception.CustomException;
import com.live_commerce.payment.application.exception.PaymentExceptionCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class DistributedLockAspect {

	private static final String REDISSON_LOCK_PREFIX = "payment:lock:";
	private final RedissonClient redissonClient;

	@Around("@annotation(distributedLock)")
	public Object lock(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) throws Throwable {
		String key = REDISSON_LOCK_PREFIX + distributedLock.key(); // 단순 문자열이면 그대로 사용

		RLock lock = redissonClient.getLock(key);
		boolean acquired = false;

		try {
			acquired = lock.tryLock(distributedLock.waitTime(), distributedLock.leaseTime(), distributedLock.timeUnit());
			if (!acquired) {
				log.warn("❌ 락 획득 실패: {}", key);
				throw new CustomException(PaymentExceptionCode.DUPLICATE_PAYMENT_IN_PROGRESS);
			}
			log.info("✅ 락 획득 성공: {}", key);
			return joinPoint.proceed();

		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new CustomException(PaymentExceptionCode.DUPLICATE_PAYMENT_IN_PROGRESS);
		} finally {
			if (acquired && lock.isHeldByCurrentThread()) {
				lock.unlock();
				log.info("🔓 락 해제 완료: {}", key);
			}
		}
	}
}

