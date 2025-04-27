package com.live_commerce.payment.infrastructure.kafka.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentCompletedEvent(
	UUID orderId,
	String message,
	BigDecimal finalPaidPrice
) {}
