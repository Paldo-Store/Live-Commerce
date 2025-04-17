package com.live_commerce.user.infrastructure.client;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.live_commerce.user.infrastructure.client.dto.CouponIssueResponse;

@FeignClient(name = "coupon", url = "http://localhost:19091", path = "/api/v1/issued-coupons")
public interface CouponClient {

	@PostMapping("/{userId}/signup-first")
	CouponIssueResponse issueFirstCoupon(@PathVariable UUID userId);
}