package com.live_commerce.order.kafkaOrder;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

//broadcast-status-response 토픽을 구독해 응답 들어야함
//응답 메시지 받기.
@Component
public class BroadcastStatusResponseConsumer {

    private final Map<String, CompletableFuture<BroadcastStatusResponseMessage>> pendingRequests = new ConcurrentHashMap<>();
    private final BroadcastStatusResponseStore broadcastStatusResponseStore;

    public BroadcastStatusResponseConsumer(BroadcastStatusResponseStore broadcastStatusResponseStore) {
        this.broadcastStatusResponseStore = broadcastStatusResponseStore;
    }

    public CompletableFuture<BroadcastStatusResponseMessage> waitForResponse(String requestId) {
        return pendingRequests.computeIfAbsent(requestId, id -> new CompletableFuture<>());
    }

    //토픽 구독 : broadcast-status-response
    //카프카 메시지 수신되면, 이 메소드 호출.
    @KafkaListener(topics = "broadcast-status-response", groupId = "order-service")
    public void listen(BroadcastStatusResponseMessage message) {

        // 응답을 store에 완료 처리
        // 응답 메시지 저장, 응답 완료 처리
        broadcastStatusResponseStore.completeResponse(message);

        // pendingRequests에서 해당 requestId의 CompletableFuture를 찾아서 완료 처리
        // 해당 future를 완료처리함.
        CompletableFuture<BroadcastStatusResponseMessage> future = pendingRequests.remove(message.requestId());
        if (future != null) {
            future.complete(message);
        }
    }
}