package com.live_commerce.order.kafkaOrder.coupon;

import java.util.UUID;

public record CouponUsedMessage(
        UUID couponId,
        UUID userId
) {

}