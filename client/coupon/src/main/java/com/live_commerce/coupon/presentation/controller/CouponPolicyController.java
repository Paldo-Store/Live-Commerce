package com.live_commerce.coupon.presentation.controller;

import com.live_commerce.coupon.application.service.CouponPolicyService;
import com.live_commerce.coupon.infrastructure.common.ResponseUtil;
import com.live_commerce.coupon.presentation.common.ApiResponse;
import com.live_commerce.coupon.presentation.dto.request.CreateCouponPolicyRequest;
import com.live_commerce.coupon.presentation.dto.response.CreateCouponPolicyResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/coupon-policies")
@AllArgsConstructor
public class CouponPolicyController {

  private final CouponPolicyService couponService;

  @PostMapping("/")
  public ResponseEntity<ApiResponse<CreateCouponPolicyResponse>> createCoupon(
      @Valid @RequestBody CreateCouponPolicyRequest request) {
    CreateCouponPolicyResponse response = couponService.createCouponPolicy(request);
    return ResponseUtil.success(response);
  }
}