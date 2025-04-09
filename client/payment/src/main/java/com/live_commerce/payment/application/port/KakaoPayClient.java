package com.live_commerce.payment.application.port;

import java.math.BigDecimal;
import java.util.UUID;

import com.live_commerce.payment.infrastructure.client.dto.KakaoPayApproveDto;
import com.live_commerce.payment.infrastructure.client.dto.KakaoPayReadyDto;

public interface KakaoPayClient {
	KakaoPayReadyDto requestKakaoPayReady(
		UUID userId,
		UUID orderId,
		BigDecimal amount
	);

	KakaoPayApproveDto requestKakaoPayApprove(
		String tid,
		String pgToken,
		String orderId,
		String userId);


}
