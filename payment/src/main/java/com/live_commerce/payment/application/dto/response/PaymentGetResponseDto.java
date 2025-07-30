package com.live_commerce.payment.application.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

import com.live_commerce.payment.domain.model.Payment;
import com.live_commerce.payment.domain.model.PaymentStatus;

public record PaymentGetResponseDto(
	UUID paymentId,
	UUID orderId,
	UUID userId,
	BigDecimal amount,
	PaymentStatus status,
	String tid
) {
	public static PaymentGetResponseDto from(Payment payment) {
		return new PaymentGetResponseDto(
			payment.getId(),
			payment.getOrderId(),
			payment.getUserId(),
			payment.getAmount(),
			payment.getStatus(),
			payment.getTid()
		);
	}
}
