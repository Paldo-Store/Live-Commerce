package com.live_commerce.payment.infrastructure.kafka.event;

import java.util.UUID;

public record PaymentFailedEvent(
	UUID orderId,
	String message
) {}
