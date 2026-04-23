package com.live_commerce.coupon.domain.exception;

import com.live_commerce.coupon.application.exception.CouponPolicyExceptionCode;
import com.live_commerce.coupon.application.exception.CustomException;

public class CouponPolicyException extends CustomException {

  public CouponPolicyException(CouponPolicyExceptionCode code) {
    super(code);
  }

  public static void forDuplicateCouponName() {
    throw new CouponPolicyException(CouponPolicyExceptionCode.DUPLICATE_COUPON_CODE);
  }

  public static void forInvalidDateRange() {
    throw new CouponPolicyException(CouponPolicyExceptionCode.INVALID_DATE_RANGE);
  }

  public static void forDiscountGreaterThanMaxOrderAmount() {
    throw new CouponPolicyException(
        CouponPolicyExceptionCode.DISCOUNT_GREATER_THAN_MAX_ORDER_AMOUNT);
  }

  public static void forDiscountGreaterThan100() {
    throw new CouponPolicyException(
        CouponPolicyExceptionCode.DISCOUNT_GREATER_THAN_100
    );
  }

  public static void forCouponPolicyNotFound(){
    throw new CouponPolicyException(CouponPolicyExceptionCode.COUPON_POLICY_NOT_FOUND);
  }

}