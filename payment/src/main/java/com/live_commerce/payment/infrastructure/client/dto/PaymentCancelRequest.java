package com.live_commerce.payment.infrastructure.client.dto;

public record PaymentCancelRequest(
	boolean success,
	String message
) {}
