package com.live_commerce.order.infrastructure.client;

import com.live_commerce.order.presentation.common.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(name = "coupon")
public interface CouponClient {

    //쿠폰 목록 조회
    @GetMapping("/{userId}")
    ApiResponse<IssuedCouponListResponse> getIssuedCoupons(@PathVariable UUID userId);

    //쿠폰 정책 조회
    @GetMapping("/api/coupons/{code}")
    ApiResponse<ReadCouponPolicyResponse> getCouponPolicy(@PathVariable("code") String code);

    /**
     * 쿠폰 사용 처리
     * @param couponId 쿠폰 ID
     */
    @PostMapping("/api/v1/coupons/{couponId}/use")
    void markCouponAsUsed(@PathVariable("couponId") UUID couponId);
}
