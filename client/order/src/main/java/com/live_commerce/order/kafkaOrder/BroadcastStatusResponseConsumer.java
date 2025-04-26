package com.live_commerce.order.kafkaOrder;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

//broadcast-status-response 토픽을 구독해 응답 들어야함
//응답 받기.
@Component
public class BroadcastStatusResponseConsumer {

    private final Map<String, CompletableFuture<BroadcastStatusResponseMessage>> pendingRequests = new ConcurrentHashMap<>();

    public CompletableFuture<BroadcastStatusResponseMessage> waitForResponse(String requestId) {
        CompletableFuture<BroadcastStatusResponseMessage> future = new CompletableFuture<>();
        pendingRequests.put(requestId, future);
        return future;
    }

    //토픽 구독 : broadcast-status-response
    @KafkaListener(topics = "broadcast-status-response", groupId = "order-service")
    public void listen(BroadcastStatusResponseMessage message) {
        CompletableFuture<BroadcastStatusResponseMessage> future = pendingRequests.remove(message.requestId());
        if (future != null) {
            future.complete(message);
        }
    }
}