package com.live_commerce.coupon.application.service;

import com.live_commerce.coupon.domain.exception.CouponPolicyException;
import com.live_commerce.coupon.domain.model.CouponPolicy;
import com.live_commerce.coupon.domain.model.DISCOUNT_TYPE;
import com.live_commerce.coupon.domain.repository.CouponPolicyRepository;
import com.live_commerce.coupon.presentation.dto.request.CreateCouponPolicyRequest;
import com.live_commerce.coupon.presentation.dto.request.UpdateCouponPolicyRequest;
import com.live_commerce.coupon.presentation.dto.response.CreateCouponPolicyResponse;
import com.live_commerce.coupon.presentation.dto.response.ReadCouponPolicyResponse;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;
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
        .orElseThrow(() -> {
          CouponPolicyException.forCouponPolicyNotFound();
          return null;
        });
    return ReadCouponPolicyResponse.fromCouponPolicy(couponPolicy);
  }

  public List<ReadCouponPolicyResponse> getCouponPolicies() {
    List<CouponPolicy> couponPolicyList = couponRepository.findByDeletedStatusFalse();
    return couponPolicyList.stream()
        .map(ReadCouponPolicyResponse::fromCouponPolicy)
        .collect(Collectors.toList());
  }

  public void deleteCouponPolicy(UUID id) {
    CouponPolicy couponPolicy = couponRepository.findById(id)
        .orElseThrow(() -> {
          CouponPolicyException.forCouponPolicyNotFound();
          return null;
        });
    couponPolicy.markCouponAsDeleted(couponPolicy.getName());
    couponRepository.save(couponPolicy);
  }

  public void updateCouponPolicy(UUID id, UpdateCouponPolicyRequest request) {
    CouponPolicy updateCouponPolicy = couponRepository.findByCodeAndDeletedStatusFalse(id)
        .orElseThrow(() -> {
          CouponPolicyException.forCouponPolicyNotFound();
          return null;
        } );

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

    updateCouponPolicy.updateCouponPolicy(
        request.name(),
        request.discountType(),
        request.discountValue(),
        request.minOrderAmt(),
        request.maxOrderAmt(),
        request.startAt(),
        request.endAt(),
        request.isActive()
    );

    couponRepository.save(updateCouponPolicy);
  }
}
