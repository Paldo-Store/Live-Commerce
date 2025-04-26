package com.live_commerce.order.kafkaOrder;

import org.springframework.stereotype.Service;

import java.util.concurrent.*;

//응답을 저장하고 기다림.
//requestId를 기준으로 응답 보관, waitForResponse를 통해 응답 기다림
@Service
public class BroadcastStatusResponseStore {

    // 요청 ID와 CompletableFuture 매핑을 위한 저장소
    private final ConcurrentHashMap<String, CompletableFuture<BroadcastStatusResponseMessage>> responseMap = new ConcurrentHashMap<>();

    // 응답을 기다리는 메서드
    public BroadcastStatusResponseMessage waitForResponse(String requestId, long timeout, TimeUnit timeUnit) throws TimeoutException, InterruptedException {
        CompletableFuture<BroadcastStatusResponseMessage> future = responseMap.get(requestId);
        if (future == null) {
            throw new IllegalStateException("응답을 찾을 수 없습니다. requestId: " + requestId);
        }
        return future.get(timeout, timeUnit);
    }

//    // 응답을 저장하는 메서드
//    public void saveResponse(String requestId, BroadcastStatusResponseMessage responseMessage) {
//        CompletableFuture<BroadcastStatusResponseMessage> future = new CompletableFuture<>();
//        future.complete(responseMessage);
//        responseMap.put(requestId, future);
//    }
//
//    // 응답을 기다리는 메서드
//    public BroadcastStatusResponseMessage waitForResponse(String requestId, long timeout, TimeUnit timeUnit) throws TimeoutException, InterruptedException {
//        CompletableFuture<BroadcastStatusResponseMessage> future = responseMap.get(requestId);
//        if (future == null) {
//            throw new IllegalStateException("응답을 찾을 수 없습니다. requestId: " + requestId);
//        }
//        return future.get(timeout, timeUnit);
//    }
//
//    // 응답을 저장하는 메서드
//    public void saveResponse(String requestId, BroadcastStatusResponseMessage responseMessage) {
//        CompletableFuture<BroadcastStatusResponseMessage> future = new CompletableFuture<>();
//        future.complete(responseMessage);
//        responseMap.put(requestId, future);
//    }
}