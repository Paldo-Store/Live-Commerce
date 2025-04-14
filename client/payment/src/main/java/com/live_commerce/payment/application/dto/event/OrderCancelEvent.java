package com.live_commerce.payment.application.dto.event;

import java.util.UUID;

public record OrderCancelEvent(
	UUID orderId,
	UUID userId,         // 선택 (필요 시)
	String reason        // 선택 (예: "ORDER_EXPIRED" 또는 "USER_CANCELED")
) {}

