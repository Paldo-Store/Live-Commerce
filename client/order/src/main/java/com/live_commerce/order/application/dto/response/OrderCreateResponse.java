package com.live_commerce.order.application.dto.response;

import com.live_commerce.order.domain.model.Order;
import com.live_commerce.order.domain.model.OrderStatus;

import java.util.UUID;

public record OrderCreateResponse (
     UUID orderId,
     UUID productId,
     Integer productQuantity,
     Long productTotalPrice,
     String requirement,
     OrderStatus status,
     UUID broadcastId
) {
    public static OrderCreateResponse of(Order order) {
        return new OrderCreateResponse(
                order.getId(),
                order.getProductId(),
                order.getProductQuantity(),
                order.getProductTotalPrice(),
                order.getRequirement(),
                order.getStatus(),
                order.getBroadcastId()
        );
    }
}
