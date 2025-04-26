package com.live_commerce.order.kafkaOrder;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

//order -> broadcast 로 요청 보냄
@Service
public class BroadcastStatusRequestProducer {
    private final KafkaTemplate<String, BroadcastStatusRequestMessage> kafkaTemplate;

    public BroadcastStatusRequestProducer(KafkaTemplate<String, BroadcastStatusRequestMessage> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void requestBroadcastStatus(UUID broadcastId, String requestId) {
        BroadcastStatusRequestMessage message = new BroadcastStatusRequestMessage(broadcastId);
        kafkaTemplate.send("broadcast-status-request", requestId, message);
    }
}
