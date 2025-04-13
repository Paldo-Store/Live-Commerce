package com.live_commerce.coupon.presentation.controller;

import com.live_commerce.coupon.application.service.IssuedCouponService;
import com.live_commerce.coupon.domain.model.IssuedCoupon;
import com.live_commerce.coupon.infrastructure.common.ResponseUtil;
import com.live_commerce.coupon.presentation.common.ApiResponse;
import com.live_commerce.coupon.presentation.dto.request.IssuedCouponRequest;
import com.live_commerce.coupon.presentation.dto.response.FirstJoinCouponResponse;
import com.live_commerce.coupon.presentation.dto.response.GetIssuedCouponResponse;
import com.live_commerce.coupon.presentation.dto.response.IssuedCouponListResponse;
import com.live_commerce.coupon.presentation.dto.response.UsedIssuedCouponResponse;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/issued-coupons")
@AllArgsConstructor
public class IssuedCouponController {

  private final IssuedCouponService issuedCouponService;

  @PostMapping("/")
  public ResponseEntity<ApiResponse<IssuedCoupon>> issueCoupon(
      @RequestBody IssuedCouponRequest request) {
    IssuedCoupon response = issuedCouponService.issueCoupon(request);
    return ResponseUtil.success(response);
  }

  // 쿠폰 사용 요쳥
  @PatchMapping("/{id}/ues")
  public ResponseEntity<ApiResponse<UsedIssuedCouponResponse>> useCoupon(@PathVariable UUID id) {
    IssuedCoupon issuedCoupon = issuedCouponService.useCoupon(id);
    UsedIssuedCouponResponse response = UsedIssuedCouponResponse.from(issuedCoupon);
    return ResponseUtil.success(response);
  }

  // 단일 쿠폰 조회
  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<GetIssuedCouponResponse>> getIssuedCoupon(
      @PathVariable UUID id) {
    GetIssuedCouponResponse response = issuedCouponService.getIssuedCoupon(id);
    return ResponseUtil.success(response);
  }

  // 쿠폰 목록 조회
  @GetMapping("/")
  public ResponseEntity<ApiResponse<IssuedCouponListResponse>> getIssuedCoupons() {
    IssuedCouponListResponse response = issuedCouponService.getIssuedCoupons();
    return ResponseUtil.success(response);
  }

  // 첫 회원가입 쿠폰 발급
  @PostMapping("/first-join")
  public ResponseEntity<ApiResponse<FirstJoinCouponResponse>> issueFirstJoinCoupon(
      UUID userId) {
    FirstJoinCouponResponse response = issuedCouponService.issueFirstJoinCoupon(userId);
    return ResponseUtil.success(response);
  }
}
