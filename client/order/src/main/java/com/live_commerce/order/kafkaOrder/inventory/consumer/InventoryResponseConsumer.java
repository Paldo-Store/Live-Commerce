package com.live_commerce.order.kafkaOrder.inventory.consumer;

import com.live_commerce.order.kafkaOrder.inventory.message.InventoryCheckResponseMessage;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import java.util.concurrent.ConcurrentHashMap;

import java.util.Map;
import java.util.concurrent.CompletableFuture;


//kafka 응답 받는 코드
//cousumer가 응답을 받아서 future를 완성시키는 역할
@Component
public class InventoryResponseConsumer {

    private final Map<String, CompletableFuture<InventoryCheckResponseMessage>> pendingRequests = new ConcurrentHashMap<>();

    // 요청에 대한 응답을 기다린다
    public CompletableFuture<InventoryCheckResponseMessage> waitForResponse(String requestId) {
        return pendingRequests.computeIfAbsent(requestId, id -> new CompletableFuture<>());
    }

    // Kafka 리스너 - inventory-response 토픽
    @KafkaListener(topics = "inventory-response", groupId = "order-service")
    public void listenInventoryResponse(InventoryCheckResponseMessage response) {
        CompletableFuture<InventoryCheckResponseMessage> future = pendingRequests.remove(response.requestId());
        if (future != null) {
            future.complete(response);
        }
    }
}