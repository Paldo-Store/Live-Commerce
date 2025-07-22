package com.live_commerce.payment.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KakaoPayReadyDto (

	String tid,

	@JsonProperty("next_redirect_pc_url")
	String nextRedirectPcUrl,

	@JsonProperty("next_redirect_mobile_url")
	String nextRedirectMobileUrl,

	@JsonProperty("created_at")
	String createdAt
){}