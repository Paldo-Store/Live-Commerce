package com.live_commerce.order.infrastructure.kafka.dto;

import java.util.UUID;

public record PaymentFailedEvent(
	UUID orderId,
	String message
) {}
