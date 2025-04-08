package com.live_commerce.coupon.presentation.dto.response;

import com.live_commerce.coupon.domain.model.CouponPolicy;
import java.util.UUID;

public record CreateCouponPolicyResponse(
    UUID id,
    String name,
    String discountType,
    String discountValue,
    String minOrderAmt,
    String maxOrderAmt,
    String startAt,
    String endAt,
    boolean isActive
) {

  public static CreateCouponPolicyResponse fromCouponPolicy(CouponPolicy couponPolicy) {
    return new CreateCouponPolicyResponse(
        couponPolicy.getCode(),
        couponPolicy.getName(),
        couponPolicy.getDiscountType().name(),
        couponPolicy.getDiscountValue().toString(),
        couponPolicy.getMinOrderAmt().toString(),
        couponPolicy.getMaxOrderAmt() != null ? couponPolicy.getMaxOrderAmt().toString() : null,
        couponPolicy.getStartAt().toString(),
        couponPolicy.getEndAt().toString(),
        couponPolicy.isActive()
    );
  }
}