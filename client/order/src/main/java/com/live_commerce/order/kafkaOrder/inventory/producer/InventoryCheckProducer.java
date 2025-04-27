package com.live_commerce.order.kafkaOrder.inventory.producer;

import com.live_commerce.order.kafkaOrder.inventory.message.InventoryCheckRequestMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

//kafka로 요청 보내는 코드
@Component
public class InventoryCheckProducer {

    private final KafkaTemplate<String, InventoryCheckRequestMessage> kafkaTemplate;

    @Value("${kafka.topic.inventory-request}")
    private String inventoryRequestTopic;

    public InventoryCheckProducer(KafkaTemplate<String, InventoryCheckRequestMessage> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendInventoryCheckRequest(InventoryCheckRequestMessage message) {
        kafkaTemplate.send(inventoryRequestTopic, message.productId().toString(), message);
    }
}