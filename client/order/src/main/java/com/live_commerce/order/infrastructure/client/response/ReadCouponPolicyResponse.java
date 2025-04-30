package com.live_commerce.order.infrastructure.client.response;

import com.live_commerce.order.domain.model.DISCOUNT_TYPE;

import java.math.BigDecimal;

public record ReadCouponPolicyResponse(
        String code,
        String name,
        DISCOUNT_TYPE discountType,
        Double discountValue,
        BigDecimal minOrderAmt,
        BigDecimal maxOrderAmt,
        String startAt,
        String endAt,
        boolean isActive
) {
}