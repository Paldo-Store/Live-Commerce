package com.live_commerce.payment.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentStatus {
	PENDING("PENDING"),       // 결제 대기
	COMPLETED("COMPLETED"),   // 결제 완료
	FAILED("FAILED"),         // 결제 실패
	REFUND("REFUND");         // 환불 완료

	private final String value;
}
