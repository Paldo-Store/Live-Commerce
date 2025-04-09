package com.live_commerce.payment.application.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.live_commerce.payment.infrastructure.client.dto.KakaoPayReadyDto;

public record PaymentReadyResponseDto(
	String tid,
	@JsonProperty("next_redirect_pc_url")
	String nextRedirectUrl
) {
	public static PaymentReadyResponseDto from(KakaoPayReadyDto dto) {
		return new PaymentReadyResponseDto(dto.tid(), dto.nextRedirectPcUrl());
	}
}
