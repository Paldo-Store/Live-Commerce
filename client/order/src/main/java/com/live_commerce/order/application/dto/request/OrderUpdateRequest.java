package com.live_commerce.order.application.dto.request;

import com.live_commerce.order.domain.model.Order;
import com.live_commerce.order.domain.model.OrderStatus;
import jakarta.validation.constraints.Min;
import lombok.Builder;

import java.util.UUID;

@Builder
public record OrderUpdateRequest(
        UUID productId,
        @Min(1) Integer productQuantity,
        String requirement
) {
    public Order toOrder(Long productTotalPrice) {
        return Order.builder()
                .productId(this.productId)
                .productQuantity(this.productQuantity)
                .productTotalPrice(productTotalPrice)
                .requirement(this.requirement)
                .build();
    }
}
