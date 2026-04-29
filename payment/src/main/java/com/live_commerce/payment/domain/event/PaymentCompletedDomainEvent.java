package com.live_commerce.payment.domain.event;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentCompletedDomainEvent(
	UUID orderId,
	BigDecimal amount
) {}
