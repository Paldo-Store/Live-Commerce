package com.live_commerce.coupon.infrastructure.client;

import com.live_commerce.coupon.presentation.dto.response.SearchCouponPolicyResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "coupon")
public interface CouponPolicyClient {
  @GetMapping("/api/v1/coupon-policies/{couponCode}")
  SearchCouponPolicyResponse getCouponPolicy(@PathVariable("couponCode") String couponCode);
}
