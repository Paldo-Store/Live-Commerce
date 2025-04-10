package com.live_commerce.order.application.dto.response;

import com.live_commerce.order.domain.model.Order;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record OrderUpdateResponse(
        UUID orderId,
        UUID productId,
        Integer productQuantity,
        Long productTotalPrice,
        String requirement,
        String status,
        LocalDateTime updatedAt
) {
    public static OrderUpdateResponse fromOrder(Order order) {
        return OrderUpdateResponse.builder()
                .orderId(order.getId())
                .productId(order.getProductId())
                .productQuantity(order.getProductQuantity())
                .productTotalPrice(order.getProductTotalPrice())
                .requirement(order.getRequirement())
                .status(order.getStatus().name())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
