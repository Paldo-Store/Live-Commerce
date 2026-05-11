package com.live_commerce.payment.application.port.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentApproveResult(
	String tid,
	BigDecimal amount,
	LocalDateTime approvedAt
) {}
