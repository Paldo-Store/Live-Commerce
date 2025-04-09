package com.live_commerce.payment.application.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PaymentReadyResponseDto(
	String tid,
	@JsonProperty("next_redirect_pc_url")
	String nextRedirectUrl
) {}
