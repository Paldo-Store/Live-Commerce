package com.live_commerce.product.inventory.application.service;

import com.live_commerce.product.inventory.domain.exception.InventoryException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class InventoryConsumer {

    private final InventoryService inventoryService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = "order-created", groupId = "inventory-group", containerFactory = "kafkaListenerContainerFactory")
    public void consumeOrderCreated(OrderCreatedEvent event) {
        log.info("order.created 이벤트 수신: {}", event);

        try {
            inventoryService.decreaseInventory(event.productId(), event.quantity());

            InventoryDecreasedEvent decreasedEvent = new InventoryDecreasedEvent(
                    event.orderId(),
                    event.productId(),
                    event.quantity()
            );
            kafkaTemplate.send("inventory-decreased", decreasedEvent);
            log.info("inventory-decreased 이벤트 발행 완료: {}", decreasedEvent);
        } catch (InventoryException e) {
            log.error("재고 차감 실패: {}", e.getMessage());
            // 실패 이벤트
            InventoryFailedEvent failedEvent = new InventoryFailedEvent(
                    event.orderId(),
                    event.productId(),
                    event.quantity(),
                    e.getMessage()
            );
            kafkaTemplate.send("inventory-failed", failedEvent);
            log.info("inventory-failed 이벤트 발행 완료: {}", failedEvent);
        }
    }

}
