package com.live_commerce.order.kafkaOrder.coupon;

import java.util.UUID;

public record CouponUsedEvent(
        UUID couponId,
        UUID userId
) {

}