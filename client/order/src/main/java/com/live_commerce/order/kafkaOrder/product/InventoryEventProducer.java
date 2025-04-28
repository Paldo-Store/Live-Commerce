package com.live_commerce.order.kafkaOrder.product;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class InventoryEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void sendOrderCreatedEvent(OrderCreatedEvent event) {
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("order-created", eventJson);
            log.info("order-created 이벤트 발행 완료: {}", eventJson);
        } catch (JsonProcessingException e) {
            log.error("order-created 이벤트 변환 실패", e);
        }
    }
}