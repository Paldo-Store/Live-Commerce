package com.live_commerce.coupon.domain.exception;

import com.live_commerce.coupon.application.exception.CouponExceptionCode;
import com.live_commerce.coupon.application.exception.CustomException;

public class CouponDiscountTypeException  extends CustomException {

  public CouponDiscountTypeException(CouponExceptionCode code) {
    super(code);
  }

    public static void forFixedDiscount(){
      throw new CouponDiscountTypeException(CouponExceptionCode.MISSING_MIN_ORDER_AMOUNT);
    }

    public static void forRateDiscount(){
      throw new CouponDiscountTypeException(CouponExceptionCode.MISSING_MAX_ORDER_AMOUNT);
    }
}
