package com.live_commerce.coupon.domain.exception;

import com.live_commerce.coupon.application.exception.CouponPolicyExceptionCode;
import com.live_commerce.coupon.application.exception.CustomException;

public class CouponDiscountTypeException  extends CustomException {

  public CouponDiscountTypeException(CouponPolicyExceptionCode code) {
    super(code);
  }

    public static void forFixedDiscount(){
      throw new CouponDiscountTypeException(CouponPolicyExceptionCode.MISSING_MIN_ORDER_AMOUNT);
    }

    public static void forRateDiscount(){
      throw new CouponDiscountTypeException(CouponPolicyExceptionCode.MISSING_MAX_ORDER_AMOUNT);
    }
}
