package com.live_commerce.payment.application.dto.request;

import java.time.LocalDateTime;
import java.util.UUID;

import com.live_commerce.payment.domain.model.PaymentStatus;

public record PaymentSearchCondition(
	UUID userId,
	UUID orderId,
	PaymentStatus status,
	LocalDateTime createdAtFrom,
	LocalDateTime createdAtTo
) {}