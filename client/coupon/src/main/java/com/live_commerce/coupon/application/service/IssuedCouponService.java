package com.live_commerce.coupon.application.service;

import com.live_commerce.coupon.domain.exception.IssuedCouponException;
import com.live_commerce.coupon.domain.model.CouponPolicy;
import com.live_commerce.coupon.domain.model.DISCOUNT_TYPE;
import com.live_commerce.coupon.domain.model.IssuedCoupon;
import com.live_commerce.coupon.domain.repository.CouponPolicyRepository;
import com.live_commerce.coupon.domain.repository.IssuedCouponRepository;
import com.live_commerce.coupon.presentation.dto.request.IssuedCouponRequest;
import com.live_commerce.coupon.presentation.dto.response.FirstJoinCouponResponse;
import com.live_commerce.coupon.presentation.dto.response.GetIssuedCouponResponse;
import com.live_commerce.coupon.presentation.dto.response.IssuedCouponListResponse;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@AllArgsConstructor
public class IssuedCouponService {

  private final IssuedCouponRepository issuedCouponRepository;
  private final CouponPolicyRepository couponPolicyRepository;

  public IssuedCoupon issueCoupon(IssuedCouponRequest request) {

    Optional<CouponPolicy> couponPolicy = couponPolicyRepository.findByCodeAndDeletedStatusFalse(
        request.couponCode());
    if (couponPolicy.isEmpty()) {
      IssuedCouponException.couponPolicyNotFound();
    }
    IssuedCoupon issuedCoupon = IssuedCoupon.from(request, couponPolicy);

    issuedCoupon = issuedCouponRepository.save(issuedCoupon);
    return issuedCoupon;
  }


  public IssuedCoupon useCoupon(UUID couponId) {

    // TODO: 사용하지 않으 쿠폰만 조회
    IssuedCoupon issuedCoupon = findIssuedCouponById(couponId);

    checkIfCouponUsed(issuedCoupon);

    return processCouponUsage(issuedCoupon);
  }

  private IssuedCoupon findIssuedCouponById(UUID couponId) {
    return issuedCouponRepository.findById(couponId)
        .orElseThrow(() -> {
          IssuedCouponException.issuedCouponNotFound();
          return null;
        });
  }

  private void checkIfCouponUsed(IssuedCoupon issuedCoupon) {
    if (issuedCoupon.getIsUsed()) {
      IssuedCouponException.alreadyUsedCoupon();
    }
  }

  private IssuedCoupon processCouponUsage(IssuedCoupon issuedCoupon) {
    issuedCoupon.useCoupon();
    return issuedCouponRepository.save(issuedCoupon);
  }

  @Transactional(readOnly = true)
  public GetIssuedCouponResponse getIssuedCoupon(UUID id) {
    IssuedCoupon issuedCoupon = findByIdAndIsUsedFalseOrIsUsedIsNull(id);
    return GetIssuedCouponResponse.from(issuedCoupon);
  }

  private IssuedCoupon findByIdAndIsUsedFalseOrIsUsedIsNull(UUID id) {
    return issuedCouponRepository.findByIdAndIsUsedFalseOrIsUsedIsNull(id)
        .orElseThrow(() ->
        {
          IssuedCouponException.issuedCouponNotFound();
          return null;
        });
  }

  @Transactional(readOnly = true)
  public IssuedCouponListResponse getIssuedCoupons() {
    //TODO : 사용자한정
    List<IssuedCoupon> issuedCoupons = issuedCouponRepository.findAll();
    return IssuedCouponListResponse.from(issuedCoupons);
  }

  public FirstJoinCouponResponse issueFirstCoupon(UUID userId) {

    String couponCode = "FIRST_COUPON";
    // TODO : 나중에는 이미 정의되어 있는 쿠폰 정책으로 쿠폰 발급이 이루어져야 함.
    CouponPolicy couponPolicy = createFirstCouponPolicy(couponCode);

    IssuedCouponRequest request = new IssuedCouponRequest(userId, couponPolicy.getCode());
    IssuedCoupon issuedCoupon = issueCoupon(request);
    return FirstJoinCouponResponse.from(issuedCoupon);
  }

  private CouponPolicy createFirstCouponPolicy(String couponCode) {

    CouponPolicy couponPolicy = CouponPolicy.builder()
        .code(couponCode)
        .name("First Coupon for Signup")
        .discountType(DISCOUNT_TYPE.FIXED)
        .discountValue(BigDecimal.valueOf(15000))
        .minOrderAmt(BigDecimal.valueOf(0))
        .maxOrderAmt(BigDecimal.valueOf(50000))
        .startAt(LocalDateTime.now())
        .endAt(LocalDateTime.now().plusYears(1))
        .isActive(true)
        .build();
    couponPolicyRepository.save(couponPolicy);
    return couponPolicy;

  }
}