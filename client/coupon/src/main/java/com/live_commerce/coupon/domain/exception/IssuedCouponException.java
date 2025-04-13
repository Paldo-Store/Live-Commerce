package com.live_commerce.coupon.domain.exception;

import com.live_commerce.coupon.application.exception.CustomException;
import com.live_commerce.coupon.application.exception.IssuedCouponExceptionCode;

public class IssuedCouponException extends CustomException {

  public IssuedCouponException(IssuedCouponExceptionCode code) {
    super(code);
  }

  public static void couponPolicyNotFound() {
    throw new IssuedCouponException(IssuedCouponExceptionCode.COUPON_POLICY_NOT_FOUND);
  }

  public static void couponAlreadyIssued() {
    throw new IssuedCouponException(IssuedCouponExceptionCode.COUPON_ALREADY_ISSUED);
  }

  public static void issuedCouponNotFound() {
    throw new IssuedCouponException(IssuedCouponExceptionCode.ISSUED_COUPON_NOT_FOUND);
  }

  public static void alreadyUsedCoupon() {
    throw new IssuedCouponException(IssuedCouponExceptionCode.ISSUED_ALREADY_USED);
  }
}
