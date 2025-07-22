package com.live_commerce.payment.infrastructure.lock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {
	String key(); // SpEL 표현식 허용 가능하게 하려면 파서 추가 필요
	TimeUnit timeUnit() default TimeUnit.SECONDS;
	long waitTime() default 5L;
	long leaseTime() default 10L;
}

