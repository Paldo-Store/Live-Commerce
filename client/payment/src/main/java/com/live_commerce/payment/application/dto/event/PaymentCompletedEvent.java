package com.live_commerce.payment.application.dto.event;

import java.util.UUID;

public record PaymentCompletedEvent(
	UUID orderId,
	UUID paymentId,
	String status,
	int totalAmount
) {}
