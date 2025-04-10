package com.live_commerce.order.application.dto.response;

import com.live_commerce.order.domain.model.Order;
import com.live_commerce.order.domain.model.OrderStatus;

import java.util.UUID;

public record OrderGetOneResponse(
        UUID orderId,
        UUID productId,
        UUID userId,
        Integer productQuantity,
        Long productTotalPrice,
        String requirement,
        OrderStatus status,
        UUID broadcastId
)
{
    public static OrderGetOneResponse of(Order order) {
        return new OrderGetOneResponse(
                order.getId(),
                order.getProductId(),
                order.getUserId(),
                order.getProductQuantity(),
                order.getProductTotalPrice(),
                order.getRequirement(),
                order.getStatus(),
                order.getBroadcastId()
        );
    }
}
