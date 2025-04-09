package com.live_commerce.order.application.dto.request;

import com.live_commerce.order.domain.model.Order;
import com.live_commerce.order.domain.model.OrderStatus;
import jakarta.validation.constraints.Min;

import java.util.UUID;

public record OrderCreateRequest (
    UUID productId,
    @Min(1) Integer productQuantity,  // 주문할 수량 (최소 1개)
    String requirement)
{
    public Order toOrder() {
        return Order.builder()
                .productId(productId)
                .productQuantity(productQuantity)
                .requirement(requirement)
                .status(OrderStatus.PENDING)
                .build();
    }
}
