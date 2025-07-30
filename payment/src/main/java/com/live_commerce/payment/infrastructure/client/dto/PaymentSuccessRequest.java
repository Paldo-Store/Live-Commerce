package com.live_commerce.payment.infrastructure.client.dto;

import java.math.BigDecimal;

public record PaymentSuccessRequest(
	boolean success,
	String message,
	BigDecimal finalPaidPrice
) {}