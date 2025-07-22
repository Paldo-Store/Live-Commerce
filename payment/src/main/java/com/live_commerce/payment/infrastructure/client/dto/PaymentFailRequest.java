package com.live_commerce.payment.infrastructure.client.dto;

public record PaymentFailRequest(
	boolean success,
	String message
) {}
