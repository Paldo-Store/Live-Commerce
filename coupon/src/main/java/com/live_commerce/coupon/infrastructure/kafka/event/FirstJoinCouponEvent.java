package com.live_commerce.coupon.infrastructure.kafka.event;

import java.util.UUID;

public record FirstJoinCouponEvent(
    UUID userId
) {

}
