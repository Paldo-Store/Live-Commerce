package com.live_commerce.payment.application.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

import com.live_commerce.payment.domain.model.Payment;

public record PaymentRefundResponseDto(
	UUID paymentId,
	UUID orderId,
	String status,
	BigDecimal amount
) {
	public static PaymentRefundResponseDto from(Payment payment) {
		return new PaymentRefundResponseDto(
			payment.getId(),
			payment.getOrderId(),
			payment.getStatus().name(),
			payment.getAmount()
		);
	}
}
