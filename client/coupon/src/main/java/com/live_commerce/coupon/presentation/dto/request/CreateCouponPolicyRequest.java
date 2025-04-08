package com.live_commerce.coupon.presentation.dto.request;

import com.live_commerce.coupon.domain.model.CouponPolicy;
import com.live_commerce.coupon.domain.model.DISCOUNT_TYPE;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreateCouponPolicyRequest(
    String name,
    DISCOUNT_TYPE discountType,
    BigDecimal discountValue,
    BigDecimal minOrderAmt,
    BigDecimal maxOrderAmt,
    LocalDateTime startAt,
    LocalDateTime endAt,
    boolean isActive
) {

  public CouponPolicy toCouponPolicy() {
    return CouponPolicy.builder()
        .name(this.name())
        .discountType(this.discountType())
        .discountValue(this.discountValue())
        .minOrderAmt(this.minOrderAmt())
        .maxOrderAmt(this.maxOrderAmt())
        .startAt(this.startAt())
        .endAt(this.endAt())
        .isActive(this.isActive())
        .build();

  }
}