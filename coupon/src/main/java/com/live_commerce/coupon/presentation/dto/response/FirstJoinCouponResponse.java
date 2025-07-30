package com.live_commerce.coupon.presentation.dto.response;

import com.live_commerce.coupon.domain.model.IssuedCoupon;
import java.util.UUID;

public record FirstJoinCouponResponse(
    UUID issuedCouponId,
    String couponCode
) {

  public static FirstJoinCouponResponse from(IssuedCoupon issuedCoupon) {
    return new FirstJoinCouponResponse(
        issuedCoupon.getId(),
        issuedCoupon.getCouponCode()
    );
  }
}