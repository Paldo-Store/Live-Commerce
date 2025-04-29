package com.live_commerce.user.infrastructure.kafka.event;

import java.util.UUID;

public record FirstJoinCouponMessage(
	UUID userId
) {
}
