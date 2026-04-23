package com.live_commerce.order.kafkaOrder.product;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class InventoryEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    //재고 감소(결제 성공시)
    public void sendOrderRequestedInventoryEvent(OrderRequestedInventoryEvent event) {
        log.info("inventory-decrease 이벤트 발행: {}", event);
        kafkaTemplate.send("inventory-decrease", event);
    }

    //재고 증가(결제 취소시)
    public void sendInventoryRollbackEvent(InventoryRollbackEvent event) {
        log.info("inventory-rollback 이벤트 발행: {}", event);
        kafkaTemplate.send("inventory-rollback", event);
    }
}