package com.live_commerce.payment.domain.model;

public enum PaymentStatus {
	PENDING,
	COMPLETED,
	FAILED,
	REFUND,
	CANCELED;

	public boolean canTransitionTo(PaymentStatus next) {
		return switch (this) {
			case PENDING -> next == COMPLETED || next == FAILED || next == CANCELED;
			case COMPLETED -> next == REFUND;
			default -> false;
		};
	}
}
