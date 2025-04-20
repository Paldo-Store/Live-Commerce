package com.live_commerce.order.infrastructure.client;

import java.time.LocalDateTime;
import java.util.UUID;

public record GetIssuedCouponResponse(
        UUID id,
        UUID userId,
        String couponCode,
        boolean isUsed,
        LocalDateTime expiresAt
) { }