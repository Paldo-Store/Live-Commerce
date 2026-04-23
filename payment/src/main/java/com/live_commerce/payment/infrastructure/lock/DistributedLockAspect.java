package com.live_commerce.payment.infrastructure.lock;

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
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
		MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
		Method method = methodSignature.getMethod();

		// @DistributedLock(key = "#dto.orderId") 등 SpEL을 파싱하기 위한 부분
		String keyExpression = distributedLock.key();
		String lockKey;
		if (keyExpression.contains("#")) {
			lockKey = REDISSON_LOCK_PREFIX + parseSpEL(method, methodSignature, joinPoint.getArgs(), keyExpression);
		} else {
			lockKey = REDISSON_LOCK_PREFIX + keyExpression;
		}

		RLock lock = redissonClient.getLock(lockKey);
		boolean acquired = false;

		try {
			acquired = lock.tryLock(distributedLock.waitTime(),
				distributedLock.leaseTime(),
				distributedLock.timeUnit());
			if (!acquired) {
				log.warn("락 획득 실패: {}", lockKey);
				throw new CustomException(PaymentExceptionCode.DUPLICATE_PAYMENT_IN_PROGRESS);
			}
			log.info("락 획득 성공: {}", lockKey);

			return joinPoint.proceed();

		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new CustomException(PaymentExceptionCode.DUPLICATE_PAYMENT_IN_PROGRESS);
		} finally {
			if (acquired && lock.isHeldByCurrentThread()) {
				lock.unlock();
				log.info("락 해제 완료: {}", lockKey);
			}
		}
	}

	private String parseSpEL(Method method, MethodSignature methodSignature,
		Object[] args, String spEl) {
		ExpressionParser parser = new SpelExpressionParser();
		Expression expression = parser.parseExpression(spEl);

		// 파라미터 이름을 가져옴
		String[] paramNames = methodSignature.getParameterNames();

		// EvaluationContext에 파라미터 이름-값을 매핑
		EvaluationContext context = new StandardEvaluationContext();
		for (int i = 0; i < paramNames.length; i++) {
			context.setVariable(paramNames[i], args[i]);
		}

		// SpEL(예: "#dto.orderId")을 실제 값으로 파싱
		return expression.getValue(context, String.class);
	}

}
