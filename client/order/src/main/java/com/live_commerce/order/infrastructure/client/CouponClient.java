package com.live_commerce.order.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(name = "Coupon")
public interface CouponClient {

    /**
     * 쿠폰 할인 금액 조회
     * @param couponId 쿠폰 ID
     * @param originalPrice 원래 총 상품 금액
     * @return 할인 금액
     */
    @GetMapping("/api/v1/coupons/{couponId}/discount")
    Long getDiscountAmount(@PathVariable("couponId") UUID couponId,
                           @RequestParam("price") Long originalPrice);

    /**
     * 쿠폰 사용 처리
     * @param couponId 쿠폰 ID
     */
    @PostMapping("/api/v1/coupons/{couponId}/use")
    void markCouponAsUsed(@PathVariable("couponId") UUID couponId);
}
