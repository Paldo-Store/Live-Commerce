package com.live_commerce.payment.infrastructure.client.dto;

public record KakaoPayCancelDto(
	String tid,
	String status,
	String partner_order_id,
	String partner_user_id,
	Amount amount,
	ApprovedCancelAmount approved_cancel_amount,
	CanceledAmount canceled_amount,
	CancelAvailableAmount cancel_available_amount,
	String canceled_at
) {
	public record Amount(int total, int tax_free, int vat, int point, int discount, int green_deposit) {}
	public record ApprovedCancelAmount(int total, int tax_free, int vat, int point, int discount, int green_deposit) {}
	public record CanceledAmount(int total, int tax_free, int vat, int point, int discount, int green_deposit) {}
	public record CancelAvailableAmount(int total, int tax_free, int vat, int point, int discount, int green_deposit) {}
}