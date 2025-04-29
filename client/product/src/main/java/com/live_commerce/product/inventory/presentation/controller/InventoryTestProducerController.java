package com.live_commerce.product.inventory.presentation.controller;


import com.live_commerce.product.inventory.infrastructure.kafka.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/inventories/test")
public class InventoryTestProducerController {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @PostMapping("/order-created")
    public String sendOrderCreatedEvent(
            @RequestParam int quantity,
            @RequestParam UUID productId
    ) {
        UUID orderId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        //UUID productId = UUID.fromString("30000000-0000-0000-0000-00000000001e");
        OrderCreatedEvent event = new OrderCreatedEvent(orderId, productId, quantity);
        kafkaTemplate.send("order-created", event);

        return "order-created 이벤트 발행 완료";
    }
}
