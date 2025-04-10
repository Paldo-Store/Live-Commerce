package com.live_commerce.coupon.application.service;

import com.live_commerce.coupon.domain.exception.IssuedCouponException;
import com.live_commerce.coupon.domain.model.IssuedCoupon;
import com.live_commerce.coupon.domain.repository.IssuedCouponRepository;
import com.live_commerce.coupon.infrastructure.client.CouponPolicyClient;
import com.live_commerce.coupon.presentation.dto.request.IssuedCouponRequest;
import com.live_commerce.coupon.presentation.dto.response.IssuedCouponResponse;
import com.live_commerce.coupon.presentation.dto.response.SearchCouponPolicyResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@AllArgsConstructor
public class IssuedCouponService {

  private final IssuedCouponRepository issuedCouponRepository;
  private final CouponPolicyClient couponPolicyClient;


  public IssuedCouponResponse issueCoupon(IssuedCouponRequest request) {

    SearchCouponPolicyResponse couponPolicy = couponPolicyClient.getCouponPolicy(request.couponCode());
    if(couponPolicy == null) {
      IssuedCouponException.couponPolicyNotFound();
    }

    IssuedCoupon issuedCoupon = IssuedCoupon.from(request);
    IssuedCoupon savedCoupon = issuedCouponRepository.save(issuedCoupon);
    return IssuedCouponResponse.fromEntity(savedCoupon);
  }

}