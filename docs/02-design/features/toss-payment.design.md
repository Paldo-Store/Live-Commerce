# Design: 토스페이먼츠 결제 수단 추가 및 PG사 추상화

**작성일**: 2026-05-11  
**대상 서비스**: `payment/`  
**참조 Plan**: `docs/01-plan/features/toss-payment.plan.md`

---

## 구현 순서

```
Step 1  PaymentGateway 포트 + 공통 Result DTO 신규
Step 2  PaymentMethod enum + Payment 도메인 모델 수정
Step 3  KakaoPayGateway 구현 (KakaoPayClientImpl 교체)
Step 4  TossPayGateway 구현
Step 5  PaymentGatewayFactory 구현
Step 6  PaymentServiceV2 의존성 교체 + DTO 수정
Step 7  KakaoPayClient 포트 및 KakaoPayClientImpl 삭제
각 Step은 독립 커밋. 이전 Step 컴파일 통과 후 다음 진행.
```

---

## 현재 구조 (변경 전)

```
PaymentServiceV2
  └─ KakaoPayClient (application/port/)   ← 카카오 전용 인터페이스
       └─ KakaoPayClientImpl (infrastructure/client/)
```

`PaymentServiceV2`가 카카오 전용 포트에 직접 의존. PG사 추가 불가.

---

## 목표 구조 (변경 후)

```
PaymentServiceV2
  └─ PaymentGatewayFactory (application/port/)   ← application 레이어에 위치
       ├─ KakaoPayGateway implements PaymentGateway
       └─ TossPayGateway   implements PaymentGateway

application/port/PaymentGateway.java        ← PG 중립 인터페이스
application/port/PaymentGatewayFactory.java ← 라우터 (infrastructure→application 위반 방지)
```

---

## Step 1: PaymentGateway 포트 + 공통 Result DTO

### `application/port/PaymentGateway.java` (신규)

```java
package com.live_commerce.payment.application.port;

import com.live_commerce.payment.application.port.dto.PaymentApproveResult;
import com.live_commerce.payment.application.port.dto.PaymentCancelResult;
import com.live_commerce.payment.application.port.dto.PaymentReadyResult;
import com.live_commerce.payment.domain.model.PaymentMethod;

import java.math.BigDecimal;
import java.util.UUID;

public interface PaymentGateway {

    boolean supports(PaymentMethod method);

    PaymentReadyResult ready(UUID userId, UUID orderId, BigDecimal amount, String itemName);

    PaymentApproveResult approve(String tid, String pgToken, String orderId, UUID userId, BigDecimal amount);

    PaymentCancelResult cancel(String tid, BigDecimal cancelAmount);
}
```

### `application/port/dto/PaymentReadyResult.java` (신규)

```java
package com.live_commerce.payment.application.port.dto;

public record PaymentReadyResult(
    String tid,
    String redirectUrl   // KakaoPay: next_redirect_pc_url, Toss: null
) {}
```

### `application/port/dto/PaymentApproveResult.java` (신규)

```java
package com.live_commerce.payment.application.port.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentApproveResult(
    String tid,
    BigDecimal amount,
    LocalDateTime approvedAt
) {}
```

### `application/port/dto/PaymentCancelResult.java` (신규)

```java
package com.live_commerce.payment.application.port.dto;

import java.math.BigDecimal;

public record PaymentCancelResult(
    String tid,
    BigDecimal canceledAmount
) {}
```

---

## Step 2: PaymentMethod enum + Payment 도메인 수정

### `domain/model/PaymentMethod.java` (신규)

```java
package com.live_commerce.payment.domain.model;

public enum PaymentMethod {
    KAKAO, TOSS
}
```

### `domain/model/Payment.java` (수정)

`paymentMethod` 컬럼 추가. 기존 레코드 호환을 위해 `DEFAULT 'KAKAO'`.

```java
@Enumerated(EnumType.STRING)
@Column(nullable = false)
private PaymentMethod paymentMethod;

// of() 팩토리 메서드에 paymentMethod 추가
public static Payment of(UUID userId, UUID orderId, BigDecimal amount, PaymentMethod paymentMethod) {
    return new Payment(userId, orderId, amount, PaymentStatus.PENDING, paymentMethod);
}
```

---

## Step 3: KakaoPayGateway (KakaoPayClientImpl 교체)

### `infrastructure/client/KakaoPayGateway.java` (신규 — 기존 KakaoPayClientImpl 내용 이전)

```java
@Component
@RequiredArgsConstructor
public class KakaoPayGateway implements PaymentGateway {

    private final RestTemplate restTemplate;
    private final RetryTemplate retryTemplate;

    @Value("${gateway.base-url}") private String gatewayBaseUrl;
    @Value("${kakao.pay.secret-key}") private String secretKey;
    @Value("${kakao.pay.cid}") private String cid;

    @Override
    public boolean supports(PaymentMethod method) {
        return method == PaymentMethod.KAKAO;
    }

    @Override
    public PaymentReadyResult ready(UUID userId, UUID orderId, BigDecimal amount, String itemName) {
        // 기존 KakaoPayClientImpl.requestKakaoPayReady() 로직 이전
        // 반환: PaymentReadyResult(tid, next_redirect_pc_url)
    }

    @Override
    public PaymentApproveResult approve(String tid, String pgToken, String orderId, String userId, BigDecimal amount) {
        // 기존 requestKakaoPayApprove() 로직 이전
        // 반환: PaymentApproveResult(tid, amount, approvedAt)
    }

    @Override
    public PaymentCancelResult cancel(String tid, BigDecimal cancelAmount) {
        // 기존 requestKakaoPayCancel() 로직 이전
        // 반환: PaymentCancelResult(tid, canceledAmount)
    }
}
```

---

## Step 4: TossPayGateway

토스페이먼츠 결제창 API v1 기준.

**인증**: `Authorization: Basic {Base64(secretKey:)}`  
**ready()**: 토스는 서버 사이드 ready API 없음 → DB 레코드만 생성, `redirectUrl = null`  
**approve()**: `POST https://api.tosspayments.com/v1/payments/confirm`  
**cancel()**: `POST https://api.tosspayments.com/v1/payments/{paymentKey}/cancel`

### `infrastructure/client/TossPayGateway.java` (신규)

```java
@Component
@RequiredArgsConstructor
public class TossPayGateway implements PaymentGateway {

    private final RestTemplate restTemplate;
    private final RetryTemplate retryTemplate;

    @Value("${toss.pay.secret-key}") private String secretKey;

    private static final String TOSS_API_BASE = "https://api.tosspayments.com/v1/payments";

    @Override
    public boolean supports(PaymentMethod method) {
        return method == PaymentMethod.TOSS;
    }

    @Override
    public PaymentReadyResult ready(UUID userId, UUID orderId, BigDecimal amount, String itemName) {
        // 토스는 클라이언트 SDK가 결제창을 열므로 서버 API 호출 없음
        // orderId를 tid로 사용 (approve 시 paymentKey로 교체)
        return new PaymentReadyResult(orderId.toString(), null);
    }

    @Override
    public PaymentApproveResult approve(String tid, String pgToken, String orderId, UUID userId, BigDecimal amount) {
        // pgToken = paymentKey (Toss 클라이언트 콜백에서 전달)
        Map<String, Object> params = Map.of(
            "paymentKey", pgToken,
            "orderId", orderId,
            "amount", amount
        );
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(params, buildHeaders());

        TossPayConfirmDto dto = retryTemplate.execute(ctx -> {
            try {
                return restTemplate.postForObject(TOSS_API_BASE + "/confirm", request, TossPayConfirmDto.class);
            } catch (HttpStatusCodeException e) {
                log.warn("[Toss] confirm 실패: orderId={}, status={}, body={}", orderId, e.getStatusCode(), e.getResponseBodyAsString());
                throw e;
            }
        });

        // null dto = Toss가 204 또는 빈 응답 반환 (비정상)
        if (dto == null) throw new IllegalStateException("Toss /confirm 응답이 비어있음: orderId=" + orderId);

        // approvedAt은 ISO 8601 오프셋 형식 "+09:00" → OffsetDateTime 파싱 필요 (LocalDateTime 직접 역직렬화 불가)
        LocalDateTime approvedAt = null;
        if (dto.approvedAt() != null) {
            try { approvedAt = OffsetDateTime.parse(dto.approvedAt()).toLocalDateTime(); }
            catch (Exception e) { log.warn("[Toss] approvedAt 파싱 실패: {}", dto.approvedAt()); }
        }
        return new PaymentApproveResult(dto.paymentKey(), dto.totalAmount(), approvedAt);
    }

    @Override
    public PaymentCancelResult cancel(String tid, BigDecimal cancelAmount) {
        Map<String, Object> params = Map.of("cancelReason", "고객 요청");
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(params, buildHeaders());

        return retryTemplate.execute(ctx -> {
            TossPayCancelDto response = restTemplate.postForObject(
                TOSS_API_BASE + "/" + tid + "/cancel", request, TossPayCancelDto.class
            );
            return new PaymentCancelResult(tid, cancelAmount);
        });
    }

    private HttpHeaders buildHeaders() {
        String encoded = Base64.getEncoder().encodeToString((secretKey + ":").getBytes());
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + encoded);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
```

### `infrastructure/client/dto/TossPayConfirmDto.java` (신규)

```java
// approvedAt은 String으로 수신 — Toss는 "+09:00" 오프셋 포함 ISO 8601 반환
// LocalDateTime으로 선언하면 Jackson 역직렬화 실패 (HttpMessageConversionException)
public record TossPayConfirmDto(
    String paymentKey,
    String orderId,
    BigDecimal totalAmount,
    String approvedAt
) {}
```

### `infrastructure/client/dto/TossPayCancelDto.java` (신규)

```java
public record TossPayCancelDto(
    String paymentKey,
    String status
) {}
```

---

## Step 5: PaymentGatewayFactory

`PaymentServiceV2`(application 레이어)가 팩토리를 주입받으므로, 팩토리는 `application/port/`에 위치해야 한다.  
`infrastructure/client/`에 두면 `application → infrastructure` 의존 방향 위반.

### `application/port/PaymentGatewayFactory.java` (신규)

```java
@Component
@RequiredArgsConstructor
public class PaymentGatewayFactory {

    private final List<PaymentGateway> gateways;

    public PaymentGateway getGateway(PaymentMethod method) {
        return gateways.stream()
            .filter(g -> g.supports(method))
            .findFirst()
            .orElseThrow(() -> new CustomException(PaymentExceptionCode.UNSUPPORTED_PAYMENT_METHOD));
    }
}
```

---

## RetryConfig 주의사항

`RetryTemplate` 기본 설정은 모든 예외를 재시도한다. 4xx 클라이언트 오류(잘못된 요청, 인증 실패 등)는 재시도해도 결과가 같으므로 `notRetryOn(HttpClientErrorException.class)` 설정 필수.

```java
RetryTemplate.builder()
    .maxAttempts(3)
    .fixedBackoff(1000)
    .notRetryOn(HttpClientErrorException.class)  // 4xx는 재시도 무의미
    .build();
```

---

## Step 6: PaymentServiceV2 수정 + DTO 수정

### `application/dto/request/PaymentReadyRequestDto.java` (수정)

```java
public record PaymentReadyRequestDto(
    UUID orderId,
    BigDecimal amount,
    String itemName,
    PaymentMethod paymentMethod   // 추가
) {
    public Payment toEntity(UUID userId) {
        return Payment.of(userId, orderId, amount, paymentMethod);
    }
}
```

### `application/dto/request/PaymentApproveRequestDto.java` (수정)

```java
public record PaymentApproveRequestDto(
    String tid,
    String pgToken,
    String orderId,
    BigDecimal amount      // 추가 — Toss 승인 시 금액 검증용
) {}
```

### `application/service/PaymentServiceV2.java` (수정)

```java
// 변경 전
private final KakaoPayClient kakaoPayClient;

// 변경 후
private final PaymentGatewayFactory gatewayFactory;
```

`readyPayment()`:
```java
PaymentGateway gateway = gatewayFactory.getGateway(dto.paymentMethod());
PaymentReadyResult result = gateway.ready(user.getUserId(), dto.orderId(), dto.amount(), dto.itemName());
payment.assignTid(result.tid());
```

`approvePayment()`:
```java
PaymentGateway gateway = gatewayFactory.getGateway(payment.getPaymentMethod());
PaymentApproveResult result;
try {
    result = gateway.approve(
        requestDto.tid(), requestDto.pgToken(), requestDto.orderId(),
        userId, requestDto.amount()
    );
} catch (RestClientException | IllegalStateException e) {
    // RestClientException: PG사 HTTP 오류
    // IllegalStateException: PG사가 빈 응답 반환 (비정상)
    paymentTxProcessor.fail(orderId, "PG사 승인 실패");
    throw new CustomException(PaymentExceptionCode.PAYMENT_APPROVE_FAIL);
}
```

`refundPaymentByOrderId()` / `compensateRefundByOrderId()`:
```java
PaymentGateway gateway = gatewayFactory.getGateway(payment.getPaymentMethod());
gateway.cancel(payment.getTid(), payment.getAmount());
```

### `application/dto/response/PaymentReadyResponseDto.java` (수정)

```java
public record PaymentReadyResponseDto(
    String tid,
    String redirectUrl      // KakaoPay: URL, Toss: null
) {
    public static PaymentReadyResponseDto from(PaymentReadyResult result) {
        return new PaymentReadyResponseDto(result.tid(), result.redirectUrl());
    }
}
```

### `application/dto/response/PaymentApproveResponseDto.java` (수정)

```java
public record PaymentApproveResponseDto(
    String tid,
    BigDecimal amount,
    LocalDateTime approvedAt
) {
    public static PaymentApproveResponseDto from(PaymentApproveResult result) {
        return new PaymentApproveResponseDto(result.tid(), result.amount(), result.approvedAt());
    }
}
```

---

## Step 7: 삭제 대상

| 파일 | 사유 |
|------|------|
| `application/port/KakaoPayClient.java` | `PaymentGateway`로 대체 |
| `infrastructure/client/KakaoPayClientImpl.java` | `KakaoPayGateway`로 대체 |
| `infrastructure/client/dto/KakaoPayReadyDto.java` | `PaymentReadyResult`로 대체 |
| `infrastructure/client/dto/KakaoPayApproveDto.java` | `PaymentApproveResult`로 대체 |
| `infrastructure/client/dto/KakaoPayCancelDto.java` | `PaymentCancelResult`로 대체 |

---

## application.yml 추가 설정

```yaml
toss:
  pay:
    secret-key: ${TOSS_PAY_SECRET_KEY}
```

---

## 레이어 의존 방향

```
presentation → application(service, port, dto) → domain
infrastructure(KakaoPayGateway, TossPayGateway) → application/port
```

`PaymentGatewayFactory`는 `application/port/`에 위치 — `application`이 주입받을 수 있음.  
`infrastructure/client/`에 두면 `application → infrastructure` 방향 위반.

---

## 완료 조건

- [x] `PaymentGateway` 포트로 카카오·토스 모두 동작
- [x] `KakaoPayClient` 포트 및 카카오 전용 DTO 삭제 완료
- [x] `Payment.paymentMethod` 저장 및 환불 시 올바른 게이트웨이 선택
- [x] 컴파일 통과
- [x] `PaymentGatewayFactory` → `application/port/`로 이동 (레이어 위반 해소)
- [x] `approve()` 파라미터 `String userId` → `UUID userId`
- [x] `TossPayConfirmDto.approvedAt` → `String` (오프셋 파싱 문제 해소)
- [x] `RetryConfig.notRetryOn(HttpClientErrorException.class)` 추가
- [x] Toss 실결제 (샌드박스) 검증 완료
