package com.live_commerce.payment.application.dto.event;

import java.util.UUID;

public record PaymentCancelEvent(
	UUID orderId,
	UUID paymentId,
	String reason
) {}

