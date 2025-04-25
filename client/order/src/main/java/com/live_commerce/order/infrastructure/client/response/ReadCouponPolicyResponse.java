package com.live_commerce.order.infrastructure.client.response;

import java.math.BigDecimal;

public record ReadCouponPolicyResponse(
        String code,
        String name,
        String discountType,
        Double discountValue,
        BigDecimal minOrderAmt,
        BigDecimal maxOrderAmt,
        String startAt,
        String endAt,
        boolean isActive
) {
}