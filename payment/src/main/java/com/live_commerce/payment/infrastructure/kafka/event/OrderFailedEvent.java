package com.live_commerce.payment.infrastructure.kafka.event;

import java.util.UUID;

public record OrderFailedEvent(
	UUID orderId,
	String message
) {}
