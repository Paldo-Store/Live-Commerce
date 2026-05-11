package com.live_commerce.payment.application.port.dto;

public record PaymentReadyResult(
	String tid,
	String redirectUrl
) {}
