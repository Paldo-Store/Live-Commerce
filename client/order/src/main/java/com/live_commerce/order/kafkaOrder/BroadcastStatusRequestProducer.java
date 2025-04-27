package com.live_commerce.order.kafkaOrder;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

//order -> broadcast 로 요청 보냄
@Service
public class BroadcastStatusRequestProducer {
    private final KafkaTemplate<String, BroadcastStatusRequestMessage> kafkaTemplate;
    private final BroadcastStatusResponseStore broadcastStatusResponseStore;

    public BroadcastStatusRequestProducer(KafkaTemplate<String, BroadcastStatusRequestMessage> kafkaTemplate, BroadcastStatusResponseStore broadcastStatusResponseStore) {
        this.kafkaTemplate = kafkaTemplate;
        this.broadcastStatusResponseStore = broadcastStatusResponseStore;
    }

    public void requestBroadcastStatus(String requestId, UUID broadcastId) {
        // 요청 ID 등록
        broadcastStatusResponseStore.registerRequest(requestId);

        BroadcastStatusRequestMessage message = new BroadcastStatusRequestMessage(requestId, broadcastId);
        kafkaTemplate.send("broadcast-status-request", requestId, message);
    }
}
