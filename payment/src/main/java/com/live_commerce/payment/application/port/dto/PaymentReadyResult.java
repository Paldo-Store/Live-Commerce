package com.live_commerce.payment.application.port.dto;

public record PaymentReadyResult(
	String tid,
	String redirectUrl
) {
	// Toss는 클라이언트 SDK가 결제창을 직접 열므로 서버 redirect 불필요 → redirectUrl = null
	public boolean requiresRedirect() {
		return redirectUrl != null;
	}
}
