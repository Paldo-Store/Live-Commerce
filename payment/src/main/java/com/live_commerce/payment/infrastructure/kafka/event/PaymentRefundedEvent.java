package com.live_commerce.payment.infrastructure.kafka.event;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentRefundedEvent(
	UUID orderId,
	BigDecimal amount
) {}
