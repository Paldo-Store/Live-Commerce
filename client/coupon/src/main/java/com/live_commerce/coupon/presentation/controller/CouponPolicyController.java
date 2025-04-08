package com.live_commerce.coupon.presentation.controller;

import com.live_commerce.coupon.application.service.CouponPolicyService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/coupon-policies")
@AllArgsConstructor
public class CouponPolicyController {

  private final CouponPolicyService couponService;


}