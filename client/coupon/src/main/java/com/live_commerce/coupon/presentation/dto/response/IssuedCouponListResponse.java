package com.live_commerce.coupon.presentation.dto.response;

import com.live_commerce.coupon.domain.model.IssuedCoupon;
import java.util.List;

public record IssuedCouponListResponse(
    List<GetIssuedCouponResponse> coupons
) {

  public static IssuedCouponListResponse from(List<IssuedCoupon> issuedCoupons) {
    return new IssuedCouponListResponse(GetIssuedCouponResponse.from(issuedCoupons));
  }
}
