package com.live_commerce.payment.infrastructure.client;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.live_commerce.payment.application.port.KakaoPayClient;
import com.live_commerce.payment.infrastructure.client.dto.KakaoPayApproveDto;
import com.live_commerce.payment.infrastructure.client.dto.KakaoPayCancelDto;
import com.live_commerce.payment.infrastructure.client.dto.KakaoPayReadyDto;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class KakaoPayClientImpl implements KakaoPayClient {

	private final RestTemplate restTemplate;

	@Value("${gateway.base-url}")
	private String gatewayBaseUrl;

	@Value("${kakao.pay.secret-key}")
	private String kakaoPaySecretKey;

	@Value("${kakao.pay.cid}")
	private String kakaoPayCid;

	@Override
	public KakaoPayReadyDto requestKakaoPayReady(
		UUID userId,
		UUID orderId,
		BigDecimal amount,
		String itemName
	) {
		// 1) JSON 형태의 파라미터 구성
		Map<String, Object> params = new HashMap<>();
		params.put("cid", kakaoPayCid);
		params.put("partner_order_id", orderId.toString());
		params.put("partner_user_id", userId.toString());
		params.put("item_name", itemName);
		params.put("quantity", 1);
		params.put("total_amount", amount.intValue());  // JSON에서는 정수로 처리하는 게 안전함
		params.put("tax_free_amount", 0);
		params.put("approval_url", gatewayBaseUrl + "/api/v1/payments/approve");
		params.put("cancel_url",   gatewayBaseUrl + "/api/v1/payments/cancel");
		params.put("fail_url",     gatewayBaseUrl + "/api/v1/payments/fail");

		// 2) 헤더 설정 (SECRET_KEY 방식)
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", "SECRET_KEY " + kakaoPaySecretKey);
		headers.setContentType(MediaType.APPLICATION_JSON);

		// 3) 요청 생성
		HttpEntity<Map<String, Object>> request = new HttpEntity<>(params, headers);

		// 4) API 호출 (변경된 URL 사용)
		ResponseEntity<KakaoPayReadyDto> response = restTemplate.postForEntity(
			"https://open-api.kakaopay.com/online/v1/payment/ready",
			request,
			KakaoPayReadyDto.class
		);

		// 5) 응답 반환
		return response.getBody();
	}

	@Override
	public KakaoPayApproveDto requestKakaoPayApprove(String tid, String pgToken, String orderId, String userId) {
		Map<String, Object> params = new HashMap<>();
		params.put("cid", kakaoPayCid);
		params.put("tid", tid);
		params.put("partner_order_id", orderId); // 실제 orderId
		params.put("partner_user_id", userId);   // 실제 userId
		params.put("pg_token", pgToken);

		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", "SECRET_KEY " + kakaoPaySecretKey);
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<Map<String, Object>> request = new HttpEntity<>(params, headers);

		ResponseEntity<KakaoPayApproveDto> response = restTemplate.postForEntity(
			"https://open-api.kakaopay.com/online/v1/payment/approve",
			request,
			KakaoPayApproveDto.class
		);

		return response.getBody();
	}

	@Override
	public KakaoPayCancelDto requestKakaoPayCancel(String tid, BigDecimal cancelAmount) {
		Map<String, Object> params = new HashMap<>();
		params.put("cid", kakaoPayCid);
		params.put("tid", tid);
		params.put("cancel_amount", cancelAmount.intValue());
		params.put("cancel_tax_free_amount", 0);

		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", "SECRET_KEY " + kakaoPaySecretKey);
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<Map<String, Object>> request = new HttpEntity<>(params, headers);

		ResponseEntity<KakaoPayCancelDto> response = restTemplate.postForEntity(
			"https://open-api.kakaopay.com/online/v1/payment/cancel",
			request,
			KakaoPayCancelDto.class
		);

		return response.getBody();
	}

}
