package com.live_commerce.user.infrastructure.kafka.dto;

import java.util.UUID;

public record FirstJoinCouponMessage(
	UUID userId
) {
}
