package com.live_commerce.coupon.presentation.dto.request;

import java.util.UUID;

public record IssuedCouponRequest(
    UUID userId,
    String couponCode
) {
}
