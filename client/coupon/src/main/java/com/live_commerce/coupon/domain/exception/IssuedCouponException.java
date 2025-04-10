package com.live_commerce.coupon.domain.exception;

import com.live_commerce.coupon.application.exception.CustomException;
import com.live_commerce.coupon.application.exception.IssuedCouponExceptionCode;

public class IssuedCouponException extends CustomException {

  public IssuedCouponException(IssuedCouponExceptionCode code) {
    super(code);
  }

  public static IssuedCouponException couponPolicyNotFound() {
    return new IssuedCouponException(IssuedCouponExceptionCode.COUPON_POLICY_NOT_FOUND);
  }

  public static IssuedCouponException couponAlreadyIssued() {
    return new IssuedCouponException(IssuedCouponExceptionCode.COUPON_ALREADY_ISSUED);
  }
}
