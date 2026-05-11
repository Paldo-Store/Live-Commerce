package com.live_commerce.payment.toss;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.live_commerce.payment.infrastructure.client.TossPayGateway;

/**
 * Toss Payments 연결 확인용 수동 테스트.
 *
 * 실행 전 준비:
 *   1. https://developers.tosspayments.com 에서 테스트 시크릿 키 발급 (test_sk_... 형식)
 *   2. VM option으로 키 주입: -Dtoss.test.secret-key=test_sk_XXXXX
 *      또는 환경변수: TOSS_TEST_SECRET_KEY=test_sk_XXXXX
 *
 * 기대 결과:
 *   - HttpClientErrorException(4xx, NOT 401) → API 연결 성공, 요청 데이터 문제
 *   - 401 Unauthorized → 시크릿 키 오류
 *   - Connection refused / UnknownHost → 네트워크 문제
 */
@Disabled("수동 실행 전용: -Dtoss.test.secret-key=test_sk_... 로 키 주입 필요")
class TossPayConnectivityTest {

    private TossPayGateway tossPayGateway;

    @BeforeEach
    void setUp() {
        String secretKey = System.getProperty("toss.test.secret-key",
            System.getenv().getOrDefault("TOSS_TEST_SECRET_KEY", ""));

        if (secretKey.isBlank()) {
            throw new IllegalStateException(
                "Toss 테스트 시크릿 키가 없습니다. -Dtoss.test.secret-key=test_sk_... 로 주입하세요."
            );
        }

        RetryTemplate retryTemplate = RetryTemplate.builder().maxAttempts(1).build();
        tossPayGateway = new TossPayGateway(new RestTemplate(), retryTemplate);
        ReflectionTestUtils.setField(tossPayGateway, "secretKey", secretKey);
    }

    @Test
    void confirm_가짜_paymentKey로_호출하면_4xx_반환() {
        // 유효한 인증 + 존재하지 않는 paymentKey → 4xx (NOT_FOUND_PAYMENT 또는 INVALID_REQUEST)
        // 401이 떨어지면 시크릿 키 오류 — 연결 확인 실패
        assertThatThrownBy(() ->
            tossPayGateway.approve(
                "fake-tid",
                "FAKE_PAYMENT_KEY_FOR_CONNECTIVITY_CHECK",
                "550e8400-e29b-41d4-a716-446655440000",
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                BigDecimal.valueOf(1000)
            )
        )
            .isInstanceOf(HttpClientErrorException.class)
            .satisfies(e -> assertThat(((HttpClientErrorException) e).getStatusCode().value())
                .as("401이면 시크릿 키 오류 — 연결 확인 불가")
                .isNotEqualTo(401));
    }
}
