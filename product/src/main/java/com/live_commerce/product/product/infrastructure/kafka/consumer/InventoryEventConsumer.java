package com.live_commerce.product.product.infrastructure.kafka.consumer;

import com.live_commerce.product.inventory.domain.exception.InventoryException;
import com.live_commerce.product.inventory.infrastructure.outbox.InventoryTxProcessor;
import com.live_commerce.product.product.infrastructure.kafka.event.InventoryRollbackEvent;
import com.live_commerce.product.product.infrastructure.kafka.event.OrderRequestedInventoryEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class InventoryEventConsumer {

    private final InventoryTxProcessor inventoryTxProcessor;

    @KafkaListener(topics = "inventory-decrease", concurrency = "4", containerFactory = "kafkaListenerContainerFactory")
    public void consumeOrderCreated(OrderRequestedInventoryEvent event) {
        log.info("inventory-decrease 이벤트 수신: {}", event);
        try {
            inventoryTxProcessor.decrease(event.orderId(), event.productId(), event.quantity());
            log.info("재고 차감 완료: orderId={}", event.orderId());
        } catch (InventoryException e) {
            log.error("재고 차감 실패: orderId={}, 이유={}", event.orderId(), e.getMessage());
            inventoryTxProcessor.fail(event.orderId(), event.productId(), event.quantity(), e.getMessage());
        }
    }

    @KafkaListener(topics = "inventory-rollback")
    public void consumeInventoryRollback(InventoryRollbackEvent event) {
        log.info("inventory-rollback 이벤트 수신: {}", event);
        try {
            inventoryTxProcessor.compensate(event.orderId(), event.productId(), event.quantity());
        } catch (Exception e) {
            log.error("재고 복구 실패 - 수동 조치 필요: orderId={}, productId={}, 이유={}", event.orderId(), event.productId(), e.getMessage());
        }
    }
}
