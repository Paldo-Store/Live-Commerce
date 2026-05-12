package com.live_commerce.payment.infrastructure.client;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.live_commerce.payment.application.port.PaymentGateway;
import com.live_commerce.payment.domain.exception.PaymentAmountMismatchException;
import com.live_commerce.payment.application.port.dto.PaymentApproveResult;
import com.live_commerce.payment.application.port.dto.PaymentReadyResult;
import com.live_commerce.payment.domain.model.PaymentMethod;
import com.live_commerce.payment.infrastructure.client.dto.KakaoPayApproveDto;
import com.live_commerce.payment.infrastructure.client.dto.KakaoPayCancelDto;
import com.live_commerce.payment.infrastructure.client.dto.KakaoPayReadyDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoPayGateway implements PaymentGateway {

	private final RestTemplate restTemplate;
	private final RetryTemplate retryTemplate;

	@Value("${gateway.base-url}")
	private String gatewayBaseUrl;

	@Value("${kakao.pay.secret-key}")
	private String secretKey;

	@Value("${kakao.pay.cid}")
	private String cid;

	@Override
	public boolean supports(PaymentMethod method) {
		return method == PaymentMethod.KAKAO;
	}

	@Override
	public PaymentReadyResult ready(UUID userId, UUID orderId, BigDecimal amount, String itemName) {
		Map<String, Object> params = new HashMap<>();
		params.put("cid", cid);
		params.put("partner_order_id", orderId.toString());
		params.put("partner_user_id", userId.toString());
		params.put("item_name", itemName);
		params.put("quantity", 1);
		params.put("total_amount", amount.longValue());
		params.put("tax_free_amount", 0);
		params.put("approval_url", gatewayBaseUrl + "/api/v2/payments/approve");
		params.put("cancel_url", gatewayBaseUrl + "/api/v2/payments/cancel");
		params.put("fail_url", gatewayBaseUrl + "/api/v2/payments/fail");

		HttpEntity<Map<String, Object>> request = new HttpEntity<>(params, buildHeaders());

		KakaoPayReadyDto dto = retryTemplate.execute(ctx -> {
			ResponseEntity<KakaoPayReadyDto> response = restTemplate.postForEntity(
				"https://open-api.kakaopay.com/online/v1/payment/ready", request, KakaoPayReadyDto.class
			);
			return response.getBody();
		});

		if (dto == null) {
			throw new IllegalStateException("카카오페이 ready 응답이 비어있음: orderId=" + orderId);
		}
		return new PaymentReadyResult(dto.tid(), dto.nextRedirectPcUrl());
	}

	@Override
	public PaymentApproveResult approve(String tid, String pgToken, String orderId, UUID userId, BigDecimal amount) {
		Map<String, Object> params = new HashMap<>();
		params.put("cid", cid);
		params.put("tid", tid);
		params.put("partner_order_id", orderId);
		params.put("partner_user_id", userId.toString());
		params.put("pg_token", pgToken);

		HttpEntity<Map<String, Object>> request = new HttpEntity<>(params, buildHeaders());

		// approve는 pg_token 일회용 → retry 금지 (재시도 시 INVALID_PG_TOKEN으로 오탐 가능)
		ResponseEntity<KakaoPayApproveDto> approveResponse = restTemplate.postForEntity(
			"https://open-api.kakaopay.com/online/v1/payment/approve", request, KakaoPayApproveDto.class
		);
		KakaoPayApproveDto dto = approveResponse.getBody();

		if (dto == null) {
			throw new IllegalStateException("카카오페이 approve 응답이 비어있음: orderId=" + orderId);
		}
		BigDecimal approvedAmount = BigDecimal.valueOf(dto.amount().total());
		if (approvedAmount.compareTo(amount) != 0) {
			log.warn("[Kakao] 금액 불일치: 요청={}, 응답={}, orderId={}", amount, approvedAmount, orderId);
			throw new PaymentAmountMismatchException(dto.tid(), "카카오 응답 금액 불일치: orderId=" + orderId);
		}
		LocalDateTime approvedAt = null;
		if (dto.approvedAt() != null) {
			try {
				approvedAt = OffsetDateTime.parse(dto.approvedAt())
					.withOffsetSameInstant(ZoneOffset.UTC)
					.toLocalDateTime();
			} catch (DateTimeParseException e) {
				log.warn("[Kakao] approvedAt 파싱 실패, null 처리: value={}", dto.approvedAt(), e);
			}
		}
		return new PaymentApproveResult(dto.tid(), approvedAmount, approvedAt);
	}

	@Override
	public void cancel(String tid, BigDecimal cancelAmount) {
		Map<String, Object> params = new HashMap<>();
		params.put("cid", cid);
		params.put("tid", tid);
		params.put("cancel_amount", cancelAmount.longValue());
		params.put("cancel_tax_free_amount", 0);

		HttpEntity<Map<String, Object>> request = new HttpEntity<>(params, buildHeaders());

		KakaoPayCancelDto dto = retryTemplate.execute(ctx -> {
			ResponseEntity<KakaoPayCancelDto> response = restTemplate.postForEntity(
				"https://open-api.kakaopay.com/online/v1/payment/cancel", request, KakaoPayCancelDto.class
			);
			return response.getBody();
		});

		if (dto == null) {
			log.warn("[Kakao] cancel 응답이 비어있음: tid={}", tid);
		}
	}

	private HttpHeaders buildHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", "SECRET_KEY " + secretKey);
		headers.setContentType(MediaType.APPLICATION_JSON);
		return headers;
	}
}
