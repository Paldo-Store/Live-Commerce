package com.live_commerce.order.application.dto.response;

import com.live_commerce.order.domain.model.Order;
import lombok.Builder;

import java.util.UUID;

@Builder
public record OrderStatusUpdateResponse(
        UUID orderId,
        String status
) {
    public static OrderStatusUpdateResponse fromOrder(Order order) {
        return OrderStatusUpdateResponse.builder()
                .orderId(order.getId())
                .status(order.getStatus().name())
                .build();
    }
}
