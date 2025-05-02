package com.live_commerce.product.product.infrastructure.kafka.consumer;

import com.live_commerce.product.inventory.application.service.InventoryService;
import com.live_commerce.product.inventory.domain.exception.InventoryException;
import com.live_commerce.product.product.infrastructure.kafka.event.InventoryDecreasedEvent;
import com.live_commerce.product.product.infrastructure.kafka.event.InventoryRollbackEvent;
import com.live_commerce.product.product.infrastructure.kafka.event.OrderRequestedInventoryEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class InventoryEventConsumer {

    private final InventoryService inventoryService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = "inventory-decrease", concurrency = "4", containerFactory = "kafkaListenerContainerFactory")
    public void consumeOrderCreated(OrderRequestedInventoryEvent event) {
        log.info("inventory-decrease 이벤트 수신: {}", event);

        try {
            inventoryService.decreaseInventoryV2(event.productId(), event.quantity());

            InventoryDecreasedEvent decreasedEvent = new InventoryDecreasedEvent(
                    event.orderId(),
                    event.productId(),
                    event.quantity()
            );
            kafkaTemplate.send("inventory-decreased", event.productId().toString(), decreasedEvent);
            log.info("inventory-decreased 이벤트 발행 완료: {}", decreasedEvent);
        } catch (InventoryException e) {
            log.error("재고 차감 실패: {}, 이유: {}", event.orderId(), e.getMessage());

//            InventoryFailedEvent failedEvent = new InventoryFailedEvent(
//                    event.orderId(),
//                    event.productId(),
//                    event.quantity(),
//                    e.getMessage()
//            );
//            kafkaTemplate.send("inventory-failed", failedEvent);
//            log.info("inventory-failed 이벤트 발행 완료: {}", failedEvent);
        }
    }

    @KafkaListener(topics = "inventory-rollback")
    public void consumeInventoryRollback(InventoryRollbackEvent event) {
        log.info("inventory-rollback 이벤트 수신: {}", event);

        try {
            inventoryService.increaseInventoryV2(event.productId(), event.quantity());

            log.info("재고 복구 완료 - productId: {}, quantity: {}", event.productId(), event.quantity());
        } catch (Exception e) {
            log.error("재고 복구 실패 - productId: {}, 이유: {}", event.productId(), e.getMessage());
            // 필요시 실패 이벤트 발행 고려 중
        }
    }


}
