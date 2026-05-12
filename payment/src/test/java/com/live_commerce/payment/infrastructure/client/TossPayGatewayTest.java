package com.live_commerce.payment.infrastructure.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import com.live_commerce.payment.domain.exception.PaymentAmountMismatchException;
import com.live_commerce.payment.application.port.dto.PaymentApproveResult;

class TossPayGatewayTest {

	private TossPayGateway gateway;
	private MockRestServiceServer mockServer;

	private static final String CONFIRM_URL = "https://api.tosspayments.com/v1/payments/confirm";
	private static final UUID USER_ID = UUID.randomUUID();
	private static final BigDecimal AMOUNT = BigDecimal.valueOf(10000);
	private static final String EXPECTED_AUTH =
		"Basic " + Base64.getEncoder().encodeToString("test_sk_dummy:".getBytes());

	@BeforeEach
	void setUp() {
		RestTemplate restTemplate = new RestTemplate();
		mockServer = MockRestServiceServer.createServer(restTemplate);
		RetryTemplate retryTemplate = RetryTemplate.builder().maxAttempts(1).build();
		gateway = new TossPayGateway(restTemplate, retryTemplate);
		ReflectionTestUtils.setField(gateway, "secretKey", "test_sk_dummy");
	}

	@Test
	void approve_성공_approvedAt_UTC_변환() {
		mockServer.expect(requestTo(CONFIRM_URL))
			.andExpect(method(HttpMethod.POST))
			.andExpect(header("Authorization", EXPECTED_AUTH))
			.andExpect(content().json("{\"paymentKey\":\"pk_test_1\",\"orderId\":\"order-1\",\"amount\":10000}"))
			.andRespond(withSuccess("""
				{"paymentKey":"pk_test_1","orderId":"order-1","totalAmount":10000,"approvedAt":"2024-06-01T15:30:00+09:00"}
				""", MediaType.APPLICATION_JSON));

		PaymentApproveResult result = gateway.approve("tid", "pk_test_1", "order-1", USER_ID, AMOUNT);

		assertThat(result.tid()).isEqualTo("pk_test_1");
		assertThat(result.amount()).isEqualByComparingTo(AMOUNT);
		// +09:00 → UTC 변환: 15:30 KST = 06:30 UTC
		assertThat(result.approvedAt()).isEqualTo(LocalDateTime.of(2024, 6, 1, 6, 30, 0));
		mockServer.verify();
	}

	@Test
	void approve_응답_금액_불일치시_예외() {
		mockServer.expect(requestTo(CONFIRM_URL))
			.andExpect(header("Authorization", EXPECTED_AUTH))
			.andRespond(withSuccess("""
				{"paymentKey":"pk1","orderId":"order-1","totalAmount":9999,"approvedAt":null}
				""", MediaType.APPLICATION_JSON));

		assertThatThrownBy(() -> gateway.approve("tid", "pk1", "order-1", USER_ID, AMOUNT))
			.isInstanceOf(PaymentAmountMismatchException.class)
			.hasMessageContaining("금액 불일치");
	}

	@Test
	void approve_빈_응답시_예외() {
		// 204 No Content → postForObject returns null
		mockServer.expect(requestTo(CONFIRM_URL))
			.andExpect(header("Authorization", EXPECTED_AUTH))
			.andRespond(withNoContent());

		assertThatThrownBy(() -> gateway.approve("tid", "pk1", "order-1", USER_ID, AMOUNT))
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("응답이 비어있음");
	}

	@Test
	void approve_approvedAt_파싱_실패시_null_처리() {
		mockServer.expect(requestTo(CONFIRM_URL))
			.andExpect(header("Authorization", EXPECTED_AUTH))
			.andRespond(withSuccess("""
				{"paymentKey":"pk1","orderId":"order-1","totalAmount":10000,"approvedAt":"not-a-date"}
				""", MediaType.APPLICATION_JSON));

		PaymentApproveResult result = gateway.approve("tid", "pk1", "order-1", USER_ID, AMOUNT);

		assertThat(result.tid()).isEqualTo("pk1");
		assertThat(result.approvedAt()).isNull();
	}
}
