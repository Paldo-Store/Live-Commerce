package com.live_commerce.payment.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KakaoPayCancelDto(
	@JsonProperty("tid") String tid,
	@JsonProperty("status") String status,
	@JsonProperty("partner_order_id") String partnerOrderId,
	@JsonProperty("partner_user_id") String partnerUserId,
	@JsonProperty("amount") Amount amount,
	@JsonProperty("approved_cancel_amount") ApprovedCancelAmount approvedCancelAmount,
	@JsonProperty("canceled_amount") CanceledAmount canceledAmount,
	@JsonProperty("cancel_available_amount") CancelAvailableAmount cancelAvailableAmount,
	@JsonProperty("canceled_at") String canceledAt
) {
	public record Amount(
		@JsonProperty("total") long total,
		@JsonProperty("tax_free") long taxFree,
		@JsonProperty("vat") long vat,
		@JsonProperty("point") long point,
		@JsonProperty("discount") long discount,
		@JsonProperty("green_deposit") long greenDeposit
	) {}

	public record ApprovedCancelAmount(
		@JsonProperty("total") long total,
		@JsonProperty("tax_free") long taxFree,
		@JsonProperty("vat") long vat,
		@JsonProperty("point") long point,
		@JsonProperty("discount") long discount,
		@JsonProperty("green_deposit") long greenDeposit
	) {}

	public record CanceledAmount(
		@JsonProperty("total") long total,
		@JsonProperty("tax_free") long taxFree,
		@JsonProperty("vat") long vat,
		@JsonProperty("point") long point,
		@JsonProperty("discount") long discount,
		@JsonProperty("green_deposit") long greenDeposit
	) {}

	public record CancelAvailableAmount(
		@JsonProperty("total") long total,
		@JsonProperty("tax_free") long taxFree,
		@JsonProperty("vat") long vat,
		@JsonProperty("point") long point,
		@JsonProperty("discount") long discount,
		@JsonProperty("green_deposit") long greenDeposit
	) {}
}
