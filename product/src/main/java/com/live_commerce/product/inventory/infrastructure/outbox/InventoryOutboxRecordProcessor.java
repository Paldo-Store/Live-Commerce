package com.live_commerce.product.inventory.infrastructure.outbox;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.live_commerce.product.inventory.domain.model.InventoryOutbox;
import com.live_commerce.product.inventory.domain.repository.InventoryOutboxRepository;
import com.live_commerce.product.product.infrastructure.kafka.event.InventoryDecreasedEvent;
import com.live_commerce.product.product.infrastructure.kafka.event.InventoryFailedEvent;
import com.live_commerce.product.product.infrastructure.kafka.event.InventorySoldOutEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryOutboxRecordProcessor {

    private static final int MAX_RETRY = 3;

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final InventoryOutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final TransactionTemplate transactionTemplate;

    public void process(InventoryOutbox outbox) {
        try {
            publish(outbox);
            outbox.markPublished();
        } catch (IllegalArgumentException e) {
            log.error("알 수 없는 이벤트 타입: {}", outbox.getEventType());
            outbox.markFailed();
        } catch (Exception e) {
            log.warn("Outbox 발행 실패 (재시도 예정): id={}, 이유={}", outbox.getId(), e.getMessage());
            outbox.incrementRetry();
            if (outbox.getRetryCount() >= MAX_RETRY) {
                log.error("Outbox 최대 재시도 초과, FAILED 처리: id={}", outbox.getId());
                outbox.markFailed();
            }
        }

        transactionTemplate.executeWithoutResult(tx ->
                outboxRepository.updateRecord(
                        outbox.getId(),
                        outbox.getStatus(),
                        outbox.getPublishedAt(),
                        outbox.getRetryCount()
                )
        );
    }

    private void publish(InventoryOutbox outbox) throws Exception {
        Map<String, Object> map = objectMapper.readValue(outbox.getPayload(), new TypeReference<>() {});

        switch (outbox.getEventType()) {
            case "INVENTORY_DECREASED" -> {
                InventoryDecreasedEvent event = objectMapper.convertValue(map, InventoryDecreasedEvent.class);
                kafkaTemplate.send("inventory-decreased", outbox.getOrderId().toString(), event);
                log.info("inventory-decreased 발행: orderId={}", outbox.getOrderId());
            }
            case "INVENTORY_FAILED" -> {
                InventoryFailedEvent event = objectMapper.convertValue(map, InventoryFailedEvent.class);
                kafkaTemplate.send("inventory-failed", outbox.getOrderId().toString(), event);
                log.info("inventory-failed 발행: orderId={}", outbox.getOrderId());
            }
            case "INVENTORY_SOLD_OUT" -> {
                InventorySoldOutEvent event = objectMapper.convertValue(map, InventorySoldOutEvent.class);
                kafkaTemplate.send("inventory-sold-out", event.productId().toString(), event);
                log.info("inventory-sold-out 발행: productId={}", event.productId());
            }
            default -> throw new IllegalArgumentException("알 수 없는 이벤트 타입: " + outbox.getEventType());
        }
    }
}
