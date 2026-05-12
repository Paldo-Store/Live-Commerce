package com.live_commerce.payment.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KakaoPayApproveDto(
	@JsonProperty("aid") String aid,
	@JsonProperty("tid") String tid,
	@JsonProperty("cid") String cid,
	@JsonProperty("partner_order_id") String partnerOrderId,
	@JsonProperty("partner_user_id") String partnerUserId,
	@JsonProperty("payment_method_type") String paymentMethodType,
	@JsonProperty("amount") Amount amount,
	@JsonProperty("approved_at") String approvedAt
) {

	public record Amount(
		@JsonProperty("total") long total,
		@JsonProperty("tax_free") long taxFree,
		@JsonProperty("vat") long vat,
		@JsonProperty("point") long point,
		@JsonProperty("discount") long discount
	) {}
}

