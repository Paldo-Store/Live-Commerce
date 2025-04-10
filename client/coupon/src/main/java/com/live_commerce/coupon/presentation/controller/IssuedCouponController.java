package com.live_commerce.coupon.presentation.controller;

import com.live_commerce.coupon.application.service.IssuedCouponService;
import com.live_commerce.coupon.infrastructure.common.ResponseUtil;
import com.live_commerce.coupon.presentation.common.ApiResponse;
import com.live_commerce.coupon.presentation.dto.request.IssuedCouponRequest;
import com.live_commerce.coupon.presentation.dto.response.IssuedCouponResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/issued-coupons")
@AllArgsConstructor
public class IssuedCouponController {

  private final IssuedCouponService issuedCouponService;

  @PostMapping("/")
  public ResponseEntity<ApiResponse<IssuedCouponResponse>> issueCoupon(
      @RequestBody IssuedCouponRequest request
  ){
    IssuedCouponResponse response = issuedCouponService.issueCoupon(request);
    return ResponseUtil.success(response);

  }
}
