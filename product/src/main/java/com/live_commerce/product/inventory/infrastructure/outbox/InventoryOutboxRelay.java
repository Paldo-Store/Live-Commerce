package com.live_commerce.product.inventory.infrastructure.outbox;

import com.live_commerce.product.inventory.domain.model.InventoryOutbox;
import com.live_commerce.product.inventory.domain.model.InventoryOutboxStatus;
import com.live_commerce.product.inventory.domain.repository.InventoryOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryOutboxRelay {

    private static final String RELAY_LOCK_KEY = "inventory-outbox-relay-lock";

    private final InventoryOutboxRepository outboxRepository;
    private final InventoryOutboxRecordProcessor recordProcessor;
    private final RedissonClient redissonClient;

    @Scheduled(fixedDelay = 3000)
    public void relay() {
        RLock lock = redissonClient.getLock(RELAY_LOCK_KEY);
        try {
            if (!lock.tryLock(0, 2, TimeUnit.MINUTES)) {
                return;
            }
            List<InventoryOutbox> pending = outboxRepository.findTop50ByStatusOrderByCreatedAt(InventoryOutboxStatus.PENDING);
            if (!pending.isEmpty()) {
                log.info("Outbox relay 시작: {} 건", pending.size());
                pending.forEach(recordProcessor::process);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Outbox relay 인터럽트 발생");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
