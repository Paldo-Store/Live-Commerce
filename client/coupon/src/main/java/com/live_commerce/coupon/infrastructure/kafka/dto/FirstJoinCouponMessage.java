package com.live_commerce.coupon.infrastructure.kafka.dto;

import java.util.UUID;

public record FirstJoinCouponMessage(
    UUID userId
) {

}
