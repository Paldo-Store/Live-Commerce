package com.live_commerce.coupon.domain.exception;

import com.live_commerce.coupon.application.exception.CouponExceptionCode;
import com.live_commerce.coupon.application.exception.CustomException;

public class CouponPolicyException extends CustomException {

  public CouponPolicyException(CouponExceptionCode code) {
    super(code);
  }

  public static void forDuplicateCouponName() {
    throw new CouponPolicyException(CouponExceptionCode.DUPLICATE_COUPON_NAME);
  }

  public static void forInvalidDateRange() {
    throw new CouponPolicyException(CouponExceptionCode.INVALID_DATE_RANGE);
  }

  public static void forDiscountGreaterThanMaxOrderAmount() {
    throw new CouponPolicyException(
        CouponExceptionCode.DISCOUNT_GREATER_THAN_MAX_ORDER_AMOUNT);
  }

  public static void forDiscountGreaterThan100() {
    throw new CouponPolicyException(
        CouponExceptionCode.DISCOUNT_GREATER_THAN_100
    );
  }

  public static void forCouponPolicyNotFound(){
    throw new CouponPolicyException(CouponExceptionCode.COUPON_POLICY_NOT_FOUND);
  }

}