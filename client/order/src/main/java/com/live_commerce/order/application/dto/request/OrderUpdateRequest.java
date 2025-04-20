package com.live_commerce.order.application.dto.request;

import com.live_commerce.order.domain.model.Order;
import com.live_commerce.order.domain.model.OrderStatus;
import jakarta.validation.constraints.Min;
import lombok.Builder;

import java.util.UUID;

@Builder
public record OrderUpdateRequest(
        //UUID productId,
        Integer productQuantity,
        String requirement,
        UUID couponId
) {
    public Order toOrder(double productTotalPrice, double finalPaidPrice) {
        return Order.builder()
                //.productId(this.productId)
                .productQuantity(this.productQuantity)
                .productTotalPrice(productTotalPrice)
                .finalPaidPrice(finalPaidPrice)
                .requirement(this.requirement)
                .couponId(this.couponId)
                .build();
    }
}
