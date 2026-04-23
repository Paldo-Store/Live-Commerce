package com.live_commerce.payment.application.dto.request;

import java.math.BigDecimal;
import java.util.UUID;

import com.live_commerce.payment.domain.model.Payment;

public record PaymentReadyRequestDto(
	UUID orderId,
	BigDecimal amount,
	String itemName
) {
	public Payment toEntity(UUID userId) {
		return Payment.of(userId, orderId, amount);
	}
}
