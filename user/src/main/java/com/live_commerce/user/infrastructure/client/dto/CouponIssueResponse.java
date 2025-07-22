package com.live_commerce.user.infrastructure.client.dto;

import java.util.UUID;

public record CouponIssueResponse(
	UUID issuedCouponId,   // 발급된 쿠폰의 ID
	String couponCode      // 쿠폰 코드
) {
}
