package com.live_commerce.coupon.application.service;

import com.live_commerce.coupon.application.exception.CouponExceptionCode;
import com.live_commerce.coupon.domain.exception.CouponPolicyException;
import com.live_commerce.coupon.domain.model.CouponPolicy;
import com.live_commerce.coupon.domain.model.DISCOUNT_TYPE;
import com.live_commerce.coupon.domain.repository.CouponPolicyRepository;
import com.live_commerce.coupon.presentation.dto.request.CreateCouponPolicyRequest;
import com.live_commerce.coupon.presentation.dto.response.CreateCouponPolicyResponse;
import com.live_commerce.coupon.presentation.dto.response.ReadCouponPolicyResponse;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Transactional
public class CouponPolicyService {

  private final CouponPolicyRepository couponRepository;

  public CreateCouponPolicyResponse createCouponPolicy(CreateCouponPolicyRequest request) {

    if (couponRepository.existsByName(request.name())) {
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

    CouponPolicy couponPolicy = request.toCouponPolicy();
    couponRepository.save(couponPolicy);
    return CreateCouponPolicyResponse.fromCouponPolicy(couponPolicy);
  }

  public ReadCouponPolicyResponse getCouponPolicy(UUID id) {
    CouponPolicy couponPolicy = couponRepository.findByCodeAndDeletedStatusFalse(id)
        .orElseThrow(() -> new CouponPolicyException(CouponExceptionCode.COUPON_POLICY_NOT_FOUND));
    return ReadCouponPolicyResponse.fromCouponPolicy(couponPolicy);
  }

}
