package com.live_commerce.payment.application.dto.request;

import java.math.BigDecimal;

public record PaymentApproveRequestDto(
	String tid,
	String pgToken,
	String orderId,
	BigDecimal amount
) {}
