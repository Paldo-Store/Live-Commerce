package com.live_commerce.payment.application.dto.request;

import java.math.BigDecimal;
import java.util.UUID;

import com.live_commerce.payment.domain.model.Payment;
import com.live_commerce.payment.domain.model.PaymentMethod;

public record PaymentReadyRequestDto(
	UUID orderId,
	BigDecimal amount,
	String itemName,
	PaymentMethod paymentMethod
) {
	public Payment toEntity(UUID userId) {
		return Payment.of(userId, orderId, amount, paymentMethod);
	}
}
