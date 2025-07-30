package com.live_commerce.payment.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.live_commerce.payment.infrastructure.client.dto.KakaoPayApproveDto;

public record PaymentApproveResponseDto(
	String tid,
	java.time.LocalDateTime approvedAt,
	BigDecimal amount
) {
	public static PaymentApproveResponseDto from(KakaoPayApproveDto dto) {
		return new PaymentApproveResponseDto(
			dto.tid(),
			LocalDateTime.parse(dto.approvedAt(), DateTimeFormatter.ISO_LOCAL_DATE_TIME),
			BigDecimal.valueOf(dto.amount().total())
		);
	}
}

