package com.live_commerce.coupon.presentation.dto.request;

import java.time.LocalDateTime;
import java.util.UUID;

public record IssuedCouponRequest(
    UUID userId,
    String couponCode,
    boolean isUsed,
    LocalDateTime usedAt,
    LocalDateTime expiresAt
) {
}
