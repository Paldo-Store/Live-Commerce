package com.live_commerce.payment.application.dto.request;

public record PaymentApproveRequestDto(
	String tid,
	String pgToken,
	String orderId
	//
) {}