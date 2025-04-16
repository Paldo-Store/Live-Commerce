package com.live_commerce.order.infrastructure.client;

public record ReadCouponPolicyResponse(
        String code,
        String name,
        String discountType,
        String discountValue,
        String minOrderAmt,
        String maxOrderAmt,
        String startAt,
        String endAt,
        boolean isActive
) {
}