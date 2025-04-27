package com.live_commerce.order.kafkaOrder.inventory.message;

//응답 메시지
import java.util.UUID;

public record InventoryCheckResponseMessage(
        String requestId,
        UUID productId,
        boolean orderAvailable  //주문 가능 여부
) {
}
