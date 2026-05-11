package com.live_commerce.payment.application.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.live_commerce.payment.application.port.dto.PaymentReadyResult;

public record PaymentReadyResponseDto(
	String tid,
	@JsonProperty("next_redirect_pc_url")
	String nextRedirectUrl
) {
	public static PaymentReadyResponseDto from(PaymentReadyResult result) {
		return new PaymentReadyResponseDto(result.tid(), result.redirectUrl());
	}
}
