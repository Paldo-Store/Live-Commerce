package com.live_commerce.order.kafkaOrder.product.producer;

import com.live_commerce.order.kafkaOrder.product.message.ProductRequestMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

//상품 요청 보냄.
@Component
public class ProductRequestProducer {

    private final KafkaTemplate<String, ProductRequestMessage> kafkaTemplate;

    @Value("${kafka.topic.product-request}")
    private String productRequestTopic;

    public ProductRequestProducer(KafkaTemplate<String, ProductRequestMessage> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    // 상품 정보를 요청하는 메서드
    // 상품 조회 요청을 kafka로 전송
    public void requestProduct(UUID productId) {
        ProductRequestMessage message = new ProductRequestMessage(productId);
        kafkaTemplate.send(productRequestTopic, productId.toString(), message);
    }
}