package com.live_commerce.order.kafkaOrder;

import org.springframework.stereotype.Component;
import java.util.concurrent.*;

//응답을 저장하고 기다림.
//requestId를 기준으로 응답 보관, waitForResponse를 통해 응답 기다림
@Component
public class BroadcastStatusResponseStore {

    // 요청 ID와 CompletableFuture 매핑을 위한 저장소
    private final ConcurrentHashMap<String, CompletableFuture<BroadcastStatusResponseMessage>> responseMap = new ConcurrentHashMap<>();

    // Kafka 리스너가 응답을 수신했을 때 호출하는 메서드
    public void completeResponse(BroadcastStatusResponseMessage responseMessage) {
        String requestId = responseMessage.requestId();
        CompletableFuture<BroadcastStatusResponseMessage> future = responseMap.get(requestId);
        if (future != null) {
            future.complete(responseMessage);  // 응답을 처리
        }
    }

    // 요청을 등록하는 메서드 (요청 보낼 때 호출)
    public void registerRequest(String requestId) {
        responseMap.put(requestId, new CompletableFuture<>());
    }

    // 응답을 기다리는 메서드는 BroadcastStatusResponseConsumer에서 처리
}
