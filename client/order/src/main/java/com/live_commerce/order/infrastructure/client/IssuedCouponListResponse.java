package com.live_commerce.order.infrastructure.client;

import java.util.List;

public record IssuedCouponListResponse(
        List<GetIssuedCouponResponse> coupons
) { }
