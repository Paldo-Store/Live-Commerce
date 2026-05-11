package com.live_commerce.payment.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record TossPayConfirmDto(
	@JsonProperty("paymentKey") String paymentKey,
	@JsonProperty("orderId") String orderId,
	@JsonProperty("totalAmount") BigDecimal totalAmount,
	@JsonProperty("approvedAt") String approvedAt
) {}
