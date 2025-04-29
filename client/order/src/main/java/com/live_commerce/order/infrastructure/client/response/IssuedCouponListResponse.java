package com.live_commerce.order.infrastructure.client.response;

import java.util.List;

public record IssuedCouponListResponse(
        List<GetIssuedCouponResponse> coupons
) { }
