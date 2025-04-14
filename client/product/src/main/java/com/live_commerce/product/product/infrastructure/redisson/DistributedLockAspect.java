package com.live_commerce.product.product.infrastructure.redisson;


import com.live_commerce.product.inventory.domain.exception.InventoryException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class DistributedLockAspect {
    private static final String REDISSON_LOCK_PREFIX = "LOCK:";
    private final RedissonClient redissonClient;

    @Around("@annotation(distributedLock)")
    public Object lock(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) throws Throwable {
        String key = REDISSON_LOCK_PREFIX + distributedLock.key();
        RLock rLock = redissonClient.getLock(key);
        boolean available = rLock.tryLock(distributedLock.waitTime(), distributedLock.leaseTime(), distributedLock.timeUnit());
        if (!available) {
            throw InventoryException.forLockAcquisitionFailed();
        }

        try {
            return joinPoint.proceed();
        } finally {
            rLock.unlock();
        }
    }

}
