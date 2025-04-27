package com.live_commerce.order.kafkaOrder.product.consumer;

import com.live_commerce.order.infrastructure.client.response.ProductCreateResponseDto;
import com.live_commerce.order.kafkaOrder.product.message.ProductResponseMessage;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ProductResponseConsumer {

    private final Map<String, CompletableFuture<ProductCreateResponseDto>> pendingRequests = new ConcurrentHashMap<>();

    // 응답을 기다리는 메서드
    public CompletableFuture<ProductCreateResponseDto> waitForResponse(String requestId) {
        return pendingRequests.computeIfAbsent(requestId, id -> new CompletableFuture<>());
    }

    // Kafka 리스너 - product-response 응답을 수신
    // 응답을 기다리는 역할
    //  order-service 에서 product-response토픽을 듣고 응답을 기다리는 쪽이다.
    @KafkaListener(topics = "product-response", groupId = "order-service")
    public void listenProductResponse(ProductResponseMessage message) {
        CompletableFuture<ProductCreateResponseDto> future = pendingRequests.remove(message.productId().toString());
        if (future != null) {
            future.complete(message.productDetails());
        }
    }
}