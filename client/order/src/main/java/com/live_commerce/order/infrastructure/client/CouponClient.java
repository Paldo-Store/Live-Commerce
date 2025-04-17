package com.live_commerce.order.infrastructure.client;

import com.live_commerce.order.presentation.common.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@FeignClient(name = "coupon", url = "http://localhost:19091")
public interface CouponClient {

    //쿠폰 목록 조회
    @GetMapping("/api/v1/issued-coupons/")
    ApiResponse<IssuedCouponListResponse> getIssuedCoupons();

    //쿠폰 정책 조회
    @GetMapping("/api/v1/coupon-policies/detail/{code}")
    ApiResponse<ReadCouponPolicyResponse> getCouponPolicy(@PathVariable("code") String code);

    /**
     * 쿠폰 사용 처리
     * @param couponId 쿠폰 ID
     */
    @PatchMapping("/api/v1/coupon-policies/{couponId}/use")
    ApiResponse<UsedIssuedCouponResponse> useCoupon(@PathVariable("couponId") UUID couponId);
}
