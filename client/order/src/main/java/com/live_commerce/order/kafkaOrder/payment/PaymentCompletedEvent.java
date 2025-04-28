package com.live_commerce.order.kafkaOrder.payment;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentCompletedEvent(
	UUID orderId,
	String message,
	BigDecimal finalPaidPrice
) {}
