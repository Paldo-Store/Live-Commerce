package com.live_commerce.coupon.presentation.dto.response;

import com.live_commerce.coupon.domain.model.DISCOUNT_TYPE;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CouponPolicySearchResult(
    UUID code,
    String name,
    DISCOUNT_TYPE discountType,
    BigDecimal discountValue,
    BigDecimal minOrderAmt,
    BigDecimal maxOrderAmt,
    LocalDateTime startAt,
    LocalDateTime endAt,
    boolean active
) {
}
