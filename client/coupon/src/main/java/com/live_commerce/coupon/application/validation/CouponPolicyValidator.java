package com.live_commerce.coupon.application.validation;

import com.live_commerce.coupon.domain.exception.CouponPolicyException;
import com.live_commerce.coupon.domain.model.DISCOUNT_TYPE;
import com.live_commerce.coupon.domain.repository.CouponPolicyRepository;
import com.live_commerce.coupon.presentation.dto.request.CreateCouponPolicyRequest;
import com.live_commerce.coupon.presentation.dto.request.UpdateCouponPolicyRequest;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class CouponPolicyValidator {

  private final CouponPolicyRepository couponPolicyRepository;

  public CouponPolicyValidator(CouponPolicyRepository couponPolicyRepository) {
    this.couponPolicyRepository = couponPolicyRepository;
  }

  public void validateForCreatePolicy(CreateCouponPolicyRequest request) {
    if (couponPolicyRepository.existsByName(request.name())) {
      CouponPolicyException.forDuplicateCouponName();
    }

    if (request.startAt().isAfter(request.endAt())) {
      CouponPolicyException.forInvalidDateRange();
    }

    if (request.discountType() == DISCOUNT_TYPE.FIXED
        && request.discountValue().compareTo(request.maxOrderAmt()) > 0) {
      CouponPolicyException.forDiscountGreaterThanMaxOrderAmount();
    }

    if (request.discountType() == DISCOUNT_TYPE.RATE
        && request.discountValue().compareTo(BigDecimal.valueOf(100)) > 0) {
      CouponPolicyException.forDiscountGreaterThan100();
    }
  }

  public void validateForUpdatePolicy(UpdateCouponPolicyRequest request) {
    if (couponPolicyRepository.existsByName(request.name())) {
      CouponPolicyException.forDuplicateCouponName();
    }

    if (request.startAt().isAfter(request.endAt())) {
      CouponPolicyException.forInvalidDateRange();
    }

    if (request.discountType() == DISCOUNT_TYPE.FIXED
        && request.discountValue().compareTo(request.maxOrderAmt()) > 0) {
      CouponPolicyException.forDiscountGreaterThanMaxOrderAmount();
    }

    if (request.discountType() == DISCOUNT_TYPE.RATE
        && request.discountValue().compareTo(BigDecimal.valueOf(100)) > 0) {
      CouponPolicyException.forDiscountGreaterThan100();
    }
  }
}
