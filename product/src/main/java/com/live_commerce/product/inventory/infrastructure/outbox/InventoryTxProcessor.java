package com.live_commerce.product.inventory.infrastructure.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.live_commerce.product.inventory.application.validation.InventoryValidator;
import com.live_commerce.product.inventory.domain.exception.InventoryException;
import com.live_commerce.product.inventory.domain.model.Inventory;
import com.live_commerce.product.inventory.domain.model.InventoryOutbox;
import com.live_commerce.product.inventory.domain.model.InventoryStatus;
import com.live_commerce.product.inventory.domain.repository.InventoryOutboxRepository;
import com.live_commerce.product.inventory.domain.repository.InventoryRepository;
import com.live_commerce.product.product.infrastructure.kafka.event.InventoryDecreasedEvent;
import com.live_commerce.product.product.infrastructure.kafka.event.InventoryFailedEvent;
import com.live_commerce.product.product.infrastructure.kafka.event.InventorySoldOutEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryTxProcessor {

    private final InventoryRepository inventoryRepository;
    private final InventoryOutboxRepository outboxRepository;
    private final InventoryValidator inventoryValidator;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 재고 차감 + Outbox 저장을 단일 트랜잭션에서 처리.
     * 차감 실패 시 예외를 던져 호출부에서 fail()을 호출하도록 함.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void decrease(UUID orderId, UUID productId, int quantity) {
        int updated = inventoryRepository.decreaseInventoryAtomically(productId, quantity);
        if (updated == 0) {
            throw InventoryException.forInventoryOutOfStock();
        }

        redisTemplate.opsForValue().increment("product:sold_count:" + productId, quantity);

        Inventory inventory = inventoryValidator.validateAndGetActiveInventory(productId);
        if (inventory.getAvailableQuantity() == 0) {
            inventory.changeStatus(InventoryStatus.OUT_OF_STOCK);
            saveOutbox(orderId, "INVENTORY_SOLD_OUT", new InventorySoldOutEvent(productId));
        }

        saveOutbox(orderId, "INVENTORY_DECREASED", new InventoryDecreasedEvent(orderId, productId, quantity));
    }

    /**
     * 재고 차감 실패 이벤트를 Outbox에 저장.
     * 별도 REQUIRES_NEW 트랜잭션으로 호출부 롤백에 영향받지 않음.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void fail(UUID orderId, UUID productId, int quantity, String message) {
        saveOutbox(orderId, "INVENTORY_FAILED", new InventoryFailedEvent(orderId, productId, quantity, message));
    }

    /**
     * 재고 복구 보상 트랜잭션 (주문 실패 시 호출).
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void compensate(UUID orderId, UUID productId, int quantity) {
        int updated = inventoryRepository.increaseInventoryAtomically(productId, quantity);
        if (updated == 0) {
            log.error("재고 복구 실패 - 대상 없음: orderId={}, productId={}", orderId, productId);
            throw InventoryException.forInventoryNotFound();
        }
        log.info("재고 복구 완료: orderId={}, productId={}, quantity={}", orderId, productId, quantity);
    }

    private void saveOutbox(UUID orderId, String eventType, Object event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            outboxRepository.save(InventoryOutbox.of(orderId, eventType, payload));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Outbox 직렬화 실패: eventType=" + eventType, e);
        }
    }
}
