package com.live_commerce.order.kafkaOrder.payment;

import java.util.UUID;

public record PaymentFailedEvent(
	UUID orderId,
	String message
) {}
