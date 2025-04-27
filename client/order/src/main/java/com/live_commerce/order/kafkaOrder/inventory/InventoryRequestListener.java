package com.live_commerce.order.kafkaOrder.inventory;

@Component
public class InventoryRequestListener {

    private final KafkaTemplate<String, InventoryCheckResponseMessage> kafkaTemplate;

    @Value("${kafka.topic.inventory-response}")
    private String inventoryResponseTopic;

    public InventoryRequestListener(KafkaTemplate<String, InventoryCheckResponseMessage> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "${kafka.topic.inventory-request}", groupId = "inventory-service")
    public void listenInventoryCheckRequest(InventoryCheckRequestMessage message) {
        boolean isAvailable = checkInventory(message.getProductId(), message.getOrderQuantity());

        InventoryCheckResponseMessage response = new InventoryCheckResponseMessage(
                message.getProductId(),
                isAvailable
        );

        kafkaTemplate.send(inventoryResponseTopic, message.getProductId().toString(), response);
    }

    // 실제 재고 조회하는 로직
    private boolean checkInventory(UUID productId, int orderQuantity) {
        // 예시: DB나 내부 메모리에서 조회
        return true; // 재고가 있으면 true, 없으면 false
    }
}