package com.live_commerce.payment.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TossPayCancelDto(
	@JsonProperty("paymentKey") String paymentKey,
	@JsonProperty("status") String status
) {}
