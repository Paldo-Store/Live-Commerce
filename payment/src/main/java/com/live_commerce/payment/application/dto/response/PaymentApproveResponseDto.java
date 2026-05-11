package com.live_commerce.payment.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.live_commerce.payment.application.port.dto.PaymentApproveResult;

public record PaymentApproveResponseDto(
	String tid,
	LocalDateTime approvedAt,
	BigDecimal amount
) {
	public static PaymentApproveResponseDto from(PaymentApproveResult result) {
		return new PaymentApproveResponseDto(result.tid(), result.approvedAt(), result.amount());
	}
}
