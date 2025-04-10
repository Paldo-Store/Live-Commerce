package com.live_commerce.coupon.presentation.controller;

import com.live_commerce.coupon.application.service.CouponPolicyService;
import com.live_commerce.coupon.infrastructure.common.ResponseUtil;
import com.live_commerce.coupon.presentation.common.ApiResponse;
import com.live_commerce.coupon.presentation.dto.request.CreateCouponPolicyRequest;
import com.live_commerce.coupon.presentation.dto.request.UpdateCouponPolicyRequest;
import com.live_commerce.coupon.presentation.dto.response.CreateCouponPolicyResponse;
import com.live_commerce.coupon.presentation.dto.response.ReadCouponPolicyResponse;
import jakarta.validation.Valid;
import java.util.List;
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

  @GetMapping("/{code}")
  public ResponseEntity<ApiResponse<ReadCouponPolicyResponse>> getCouponPolicy(
      @PathVariable("code") String code) {
    ReadCouponPolicyResponse response = couponService.getCouponPolicy(code);
    return ResponseUtil.success(response);
  }

  @GetMapping("/")
  public ResponseEntity<ApiResponse<List<ReadCouponPolicyResponse>>> getCouponPolicies() {
    List<ReadCouponPolicyResponse> response = couponService.getCouponPolicies();
    return ResponseUtil.success(response);
  }

  @DeleteMapping("/{code}")
  public ResponseEntity<ApiResponse<Void>> deleteCouponPolicy(@PathVariable String code) {
    couponService.deleteCouponPolicy(code);
    return ResponseUtil.noContent();
  }

  @PatchMapping("/{code}")
  public ResponseEntity<ApiResponse<Void>> updateCouponPolicy(
      @PathVariable String code,
      @RequestBody UpdateCouponPolicyRequest request
  ) {
    couponService.updateCouponPolicy(code, request);
    return ResponseUtil.noContent();
  }

}