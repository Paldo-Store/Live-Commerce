package com.live_commerce.payment.domain.exception;

public class PaymentAmountMismatchException extends RuntimeException {

	private final String confirmedTid;

	public PaymentAmountMismatchException(String confirmedTid, String message) {
		super(message);
		this.confirmedTid = confirmedTid;
	}

	public String getConfirmedTid() {
		return confirmedTid;
	}
}
