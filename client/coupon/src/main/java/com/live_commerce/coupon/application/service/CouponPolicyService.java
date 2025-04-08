package com.live_commerce.coupon.application.service;

import com.live_commerce.coupon.domain.repository.CouponPolicyRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Transactional
public class CouponPolicyService {

  private final CouponPolicyRepository couponRepository;

}
