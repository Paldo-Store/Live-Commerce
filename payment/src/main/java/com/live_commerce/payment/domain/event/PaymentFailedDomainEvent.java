package com.live_commerce.payment.domain.event;

import java.util.UUID;

public record PaymentFailedDomainEvent(
	UUID orderId,
	String reason
) {}
