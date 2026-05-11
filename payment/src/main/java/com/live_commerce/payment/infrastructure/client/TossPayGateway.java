package com.live_commerce.payment.infrastructure.client;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.live_commerce.payment.application.exception.PaymentAmountMismatchException;
import com.live_commerce.payment.application.port.PaymentGateway;
import com.live_commerce.payment.application.port.dto.PaymentApproveResult;
import com.live_commerce.payment.application.port.dto.PaymentReadyResult;
import com.live_commerce.payment.domain.model.PaymentMethod;
import com.live_commerce.payment.infrastructure.client.dto.TossPayCancelDto;
import com.live_commerce.payment.infrastructure.client.dto.TossPayConfirmDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.HttpStatusCodeException;

@Slf4j
@Component
@RequiredArgsConstructor
public class TossPayGateway implements PaymentGateway {

	private final RestTemplate restTemplate;
	private final RetryTemplate retryTemplate;

	@Value("${toss.pay.secret-key}")
	private String secretKey;

	private static final String TOSS_API_BASE = "https://api.tosspayments.com/v1/payments";

	@Override
	public boolean supports(PaymentMethod method) {
		return method == PaymentMethod.TOSS;
	}

	@Override
	public PaymentReadyResult ready(UUID userId, UUID orderId, BigDecimal amount, String itemName) {
		return new PaymentReadyResult(orderId.toString(), null);
	}

	@Override
	public PaymentApproveResult approve(String tid, String pgToken, String orderId, UUID userId, BigDecimal amount) {
		Map<String, Object> params = Map.of(
			"paymentKey", pgToken,
			"orderId", orderId,
			"amount", amount.longValue()
		);

		HttpEntity<Map<String, Object>> request = new HttpEntity<>(params, buildHeaders());

		TossPayConfirmDto dto = retryTemplate.execute(ctx -> {
			try {
				return restTemplate.postForObject(TOSS_API_BASE + "/confirm", request, TossPayConfirmDto.class);
			} catch (HttpStatusCodeException e) {
				log.warn("[Toss] confirm 실패: orderId={}, status={}", orderId, e.getStatusCode());
				throw e;
			}
		});

		if (dto == null) {
			throw new IllegalStateException("Toss /confirm 응답이 비어있음: orderId=" + orderId);
		}
		if (dto.totalAmount() == null || dto.totalAmount().compareTo(amount) != 0) {
			log.warn("[Toss] 금액 불일치: 요청={}, 응답={}, orderId={}", amount, dto.totalAmount(), orderId);
			throw new PaymentAmountMismatchException(dto.paymentKey(), "Toss 응답 금액 불일치: orderId=" + orderId);
		}
		LocalDateTime approvedAt = null;
		if (dto.approvedAt() != null) {
			try {
				approvedAt = OffsetDateTime.parse(dto.approvedAt())
					.withOffsetSameInstant(ZoneOffset.UTC)
					.toLocalDateTime();
			} catch (DateTimeParseException e) {
				log.warn("[Toss] approvedAt 파싱 실패, null 처리: value={}", dto.approvedAt(), e);
			}
		}
		return new PaymentApproveResult(dto.paymentKey(), dto.totalAmount(), approvedAt);
	}

	@Override
	public void cancel(String tid, BigDecimal cancelAmount) {
		Map<String, Object> params = Map.of("cancelReason", "고객 요청", "cancelAmount", cancelAmount.longValue());

		HttpEntity<Map<String, Object>> request = new HttpEntity<>(params, buildHeaders());

		TossPayCancelDto result = retryTemplate.execute(ctx ->
			restTemplate.postForObject(TOSS_API_BASE + "/" + tid + "/cancel", request, TossPayCancelDto.class)
		);
		if (result == null) {
			log.warn("[Toss] cancel 응답이 비어있음: tid={}", tid);
		}
	}

	private HttpHeaders buildHeaders() {
		String encoded = Base64.getEncoder().encodeToString((secretKey + ":").getBytes());
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", "Basic " + encoded);
		headers.setContentType(MediaType.APPLICATION_JSON);
		return headers;
	}
}
