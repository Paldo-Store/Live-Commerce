package com.live_commerce.payment.application.dto.response;

import java.math.BigDecimal;

public record PaymentApproveResponseDto(
	String tid,
	java.time.LocalDateTime approvedAt,
	BigDecimal amount
) {
}

