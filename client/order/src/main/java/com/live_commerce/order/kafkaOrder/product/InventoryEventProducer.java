package com.live_commerce.order.kafkaOrder.product;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.core.KafkaTemplate;

import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String ORDER_CREATED_TOPIC = "order-created"; // ✅ 재고 컨슈머가 듣는 토픽 이름 정확히!

    public void sendOrderCreatedEvent(OrderCreatedEvent event) {
        kafkaTemplate.send(ORDER_CREATED_TOPIC, event); // ✅ Object로 그대로 보내야 JsonDeserializer가 파싱 가능
        log.info("✅ Kafka 이벤트 발행 완료: topic={}, payload={}", ORDER_CREATED_TOPIC, event);
    }
}
