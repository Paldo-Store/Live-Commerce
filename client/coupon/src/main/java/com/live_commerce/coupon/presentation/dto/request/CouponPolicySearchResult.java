package com.live_commerce.coupon.presentation.dto.request;

import com.live_commerce.coupon.domain.model.DISCOUNT_TYPE;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CouponPolicySearchResult(
    String code,
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
