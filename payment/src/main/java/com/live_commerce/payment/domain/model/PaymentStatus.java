package com.live_commerce.payment.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentStatus {
	PENDING("PENDING"),
	COMPLETED("COMPLETED"),
	FAILED("FAILED"),
	REFUND("REFUND"),
	CANCELED("CANCELED");

	private final String value;

	public boolean canTransitionTo(PaymentStatus next) {
		return switch (this) {
			case PENDING -> next == COMPLETED || next == FAILED || next == CANCELED;
			case COMPLETED -> next == REFUND;
			default -> false;
		};
	}
}
