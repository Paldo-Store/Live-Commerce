package com.live_commerce.order.infrastructure.client.response;


import java.time.LocalDateTime;
import java.util.UUID;

public record UsedIssuedCouponResponse(
        UUID issuedCouponId,
        String couponCode,
        UUID userId,
        boolean isUsed,
        LocalDateTime usedAt,
        LocalDateTime expiresAt
) {}