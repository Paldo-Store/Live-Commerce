# Design: 결제 트랜잭션 정합성 개선

**작성일**: 2026-04-20  
**대상 서비스**: `payment/`  
**참조 Plan**: `docs/01-plan/features/payment-transaction-integrity.plan.md`

---

## 구현 순서

```
Step 1 (T4) → Step 2 (T1) → Step 3 (T3) → Step 4 (T2)
각 Step은 독립 커밋. 이전 Step 컴파일 통과 후 다음 진행.
```

---

## Step 1: T4 — 유효시간 상수화

### 문제

`PaymentServiceV2`의 `10, TimeUnit.MINUTES`가 하드코딩. `PaymentExpirationListener`는 key prefix를 별도로 하드코딩.

### 신규 파일

**`infrastructure/redis/PaymentRedisKeys.java`**
```java
package com.live_commerce.payment.infrastructure.redis;

public final class PaymentRedisKeys {
    public static final String EXPIRE_KEY_PREFIX = "payment:expire:";
    private PaymentRedisKeys() {}
}
```

Redis key 문자열을 한 곳에서 관리. `PaymentServiceV2`, `PaymentExpirationListener`, `PaymentDomainEventListener` 모두 이 상수 참조.

### `application.yml` 변경

```yaml
payment:
  expire-minutes: 10
```

기존 `spring:` 블록 아래에 추가.

### `PaymentServiceV2.java` 변경

```java
// 제거
private static final String PAYMENT_EXPIRE_KEY_PREFIX = "payment:expire:";

// 사용처 변경
// 변경 전: PAYMENT_EXPIRE_KEY_PREFIX + dto.orderId()
// 변경 후: PaymentRedisKeys.EXPIRE_KEY_PREFIX + dto.orderId()
```

만료 시간(`paymentExpireMinutes`)은 `PaymentDomainEventListener`가 단독 소유 — V2에 `@Value` 주입 없음.

### `PaymentExpirationListener.java` 변경

```java
// 변경 전: expiredKey.startsWith("payment:expire:")
// 변경 후: expiredKey.startsWith(PaymentRedisKeys.EXPIRE_KEY_PREFIX)

// 변경 전: UUID.fromString(expiredKey.replace("payment:expire:", ""))
// 변경 후: UUID.fromString(expiredKey.replace(PaymentRedisKeys.EXPIRE_KEY_PREFIX, ""))
```

### 완료 조건

- `PaymentRedisKeys.EXPIRE_KEY_PREFIX` 사용으로 통일
- `application.yml`의 `payment.expire-minutes` 값으로 만료 시간 제어 가능
- 컴파일 통과

---

## Step 2: T1 — 결제 만료 검증

### 문제

`approvePayment`에서 Redis key 존재 여부를 확인하지 않아, 10분이 지난 결제도 승인 가능.

### `PaymentExceptionCode.java` 변경

```java
PAYMENT_EXPIRED(HttpStatus.GONE, "결제 유효시간이 만료되었습니다.");
```

### `PaymentServiceV2.approvePayment()` 변경

기존 `@Transactional` 유지 상태에서 검증 로직만 추가 (Step 4에서 `@Transactional` 제거):

```java
// PENDING 상태 체크 직후에 추가
RBucket<String> expireBucket = redissonClient.getBucket(
    PaymentRedisKeys.EXPIRE_KEY_PREFIX + requestDto.orderId()
);
if (!expireBucket.isExists()) {
    throw new CustomException(PaymentExceptionCode.PAYMENT_EXPIRED);
}
```

### 완료 조건

- 만료된 결제(Redis key 없음) 승인 시도 → HTTP 410 `PAYMENT_EXPIRED` 응답
- 미만료 결제 → 기존 흐름 동일

---

## Step 3: T3 — readyPayment Redis를 AFTER_COMMIT으로 분리

### 문제

`readyPayment`의 `@Transactional` 내에서 `bucket.set()` 직접 호출 → Redis는 DB 트랜잭션에 미참여 → 정합성 불일치 가능.

### 신규 파일

**`domain/event/PaymentReadyDomainEvent.java`**
```java
package com.live_commerce.payment.domain.event;

import java.util.UUID;

public record PaymentReadyDomainEvent(UUID orderId, UUID paymentId) {}
```

### `PaymentServiceV2.readyPayment()` 변경

```java
// 제거
RBucket<String> bucket = redissonClient.getBucket(PaymentRedisKeys.EXPIRE_KEY_PREFIX + dto.orderId());
bucket.set(payment.getId().toString(), paymentExpireMinutes, TimeUnit.MINUTES);

// 추가 (paymentRepository.save() 이후)
eventPublisher.publishEvent(new PaymentReadyDomainEvent(dto.orderId(), payment.getId()));
```

`@Transactional` + `eventPublisher.publishEvent()` → `@TransactionalEventListener(AFTER_COMMIT)` 리스너가 DB 커밋 완료 후 Redis 설정.

### `PaymentDomainEventListener.java` 변경

```java
// 추가 필드
private final PaymentCachePort paymentCachePort;  // 생성자 주입

// 추가 핸들러
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
public void onPaymentReady(PaymentReadyDomainEvent event) {
    paymentCachePort.setExpiry(event.orderId(), event.paymentId());
}
```

Redis key 구성·만료 시간은 `PaymentCacheAdapter` 내부로 캡슐화 — Listener는 Port 인터페이스만 알면 됨.

### `PaymentServiceV2` 필드 정리

`readyPayment`에서 `RedissonClient`를 직접 사용하지 않게 되면:
- `readyPayment`는 `RedissonClient` 불필요 → 하지만 Step 2에서 `approvePayment`가 여전히 `redissonClient` 사용
- Step 4 완료 후에도 `approvePayment`에서 Redis key 검증에 사용하므로 **필드 유지**

### 완료 조건

- `readyPayment` 내 `bucket.set()` 직접 호출 없음
- DB 커밋 후에만 Redis key 설정 (DB 롤백 시 Redis key 미설정)
- Redis set 실패 시 `RetryTemplate` 3회 재시도 후 error log — TTL 이벤트가 발생하지 않아 `PaymentExpirationListener` 동작 불가. fallback 없음

---

## Step 4: T2 — approvePayment 외부 API / DB 트랜잭션 분리

### 문제

`approvePayment`가 `@Transactional`이므로 카카오 HTTP 호출 동안 DB 커넥션 점유 + 카카오 성공 후 DB 실패 시 정합성 오류.

### `PaymentServiceV2.java` 변경

**추가 필드**
```java
private final PaymentTxProcessor paymentTxProcessor;
```

**`approvePayment` 재설계**

```java
// @Transactional 제거
public PaymentApproveResponseDto approvePayment(PaymentApproveRequestDto requestDto, UUID userId) {
    UUID orderId = UUID.fromString(requestDto.orderId());

    // ── 1. 사전 검증 [no tx] ──────────────────────────────────
    Payment payment = paymentRepository.findByOrderId(orderId)
        .orElseThrow(() -> new CustomException(PaymentExceptionCode.NOT_FOUND));

    if (payment.getStatus() != PaymentStatus.PENDING) {
        throw new CustomException(PaymentExceptionCode.INVALID_STATUS);
    }

    RBucket<String> expireBucket = redissonClient.getBucket(
        PaymentRedisKeys.EXPIRE_KEY_PREFIX + orderId
    );
    if (!expireBucket.isExists()) {
        throw new CustomException(PaymentExceptionCode.PAYMENT_EXPIRED);
    }

    // ── 2. 카카오 API 호출 [no tx, 커넥션 미점유] ──────────────
    KakaoPayApproveDto approveDto;
    try {
        approveDto = kakaoPayClient.requestKakaoPayApprove(
            requestDto.tid(), requestDto.pgToken(), requestDto.orderId(), userId.toString()
        );
    } catch (RestClientException e) {
        paymentTxProcessor.fail(orderId, "카카오페이 승인 실패");
        throw new CustomException(PaymentExceptionCode.PAYMENT_APPROVE_FAIL);
    }

    // ── 3. DB 상태 업데이트 [REQUIRES_NEW tx] ──────────────────
    try {
        paymentTxProcessor.complete(orderId);
    } catch (RuntimeException e) {
        log.error("[Payment] DB 업데이트 실패 - 카카오 보상 취소 시작: orderId={}", orderId, e);
        try {
            kakaoPayClient.requestKakaoPayCancel(payment.getTid(), payment.getAmount());
        } catch (RestClientException ex) {
            log.error("[Payment] 보상 취소 실패 - 수동 처리 필요: orderId={}", orderId, ex);
        }
        throw new CustomException(PaymentExceptionCode.PAYMENT_APPROVE_FAIL);
    }

    // ── 4. Redis key 삭제 [no tx] ─────────────────────────────
    expireBucket.delete();

    return PaymentApproveResponseDto.from(approveDto);
}
```

### PaymentTxProcessor

`@Transactional(propagation = Propagation.REQUIRES_NEW)`를 메서드 단위로 선언한 별도 Spring Bean. `PaymentServiceV2`가 직접 트랜잭션을 제어하지 않고 이 Bean에 위임한다.

```java
@Service
public class PaymentTxProcessor {
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void fail(UUID orderId, String reason) { ... }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void complete(UUID orderId) { ... }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Payment refund(UUID orderId) { ... }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void cancel(UUID orderId) { ... }
}
```

Spring AOP는 메서드 전체를 감싸는 구조라 하나의 메서드 안에서 구간별로 트랜잭션을 제어할 수 없다. 별도 Bean으로 분리해야 프록시가 `REQUIRES_NEW` 전파를 보장한다. (같은 클래스 내 `private` 메서드 분리는 self-invocation으로 프록시 우회 — 동작 안 함)

### 완료 조건

- `approvePayment`에 `@Transactional` 없음
- 카카오 API 호출 구간에서 DB 커넥션 미점유
- 카카오 성공 → DB 실패 시 카카오 취소 보상 로직 실행 + 에러 로그

---

## 전체 파일 변경 요약

### 신규 생성

| 파일 | 설명 |
|------|------|
| `infrastructure/redis/PaymentRedisKeys.java` | Redis key prefix 상수 — `EXPIRE_KEY_PREFIX = "payment:expire:"` |
| `application/service/PaymentTxProcessor.java` | `REQUIRES_NEW` 전파 트랜잭션 처리 Bean |
| `domain/event/PaymentReadyDomainEvent.java` | 결제 준비 도메인 이벤트 |
| `domain/event/PaymentCompletedDomainEvent.java` | 결제 완료 도메인 이벤트 |
| `domain/event/PaymentFailedDomainEvent.java` | 결제 실패 도메인 이벤트 |

### 수정

| 파일 | 변경 내용 |
|------|----------|
| `application.yml` | `payment.expire-minutes: 10` 추가 |
| `application/exception/PaymentExceptionCode.java` | `PAYMENT_EXPIRED` (HTTP 410) 추가 |
| `application/service/PaymentServiceV2.java` | `PaymentTxProcessor` 주입, `approvePayment` 트랜잭션 분리, Redis 만료 검증 추가 |
| `infrastructure/listener/PaymentExpirationListener.java` | `PaymentRedisKeys` 상수 참조, `payment.fail()` 도메인 메서드 사용 |
| `infrastructure/kafka/listener/PaymentDomainEventListener.java` | `onPaymentReady` AFTER_COMMIT 핸들러 추가 (`RetryTemplate` 3회 재시도) |

---

## 리스크 및 대응

| 리스크 | 대응 |
|--------|------|
| Step 3: Redis AFTER_COMMIT 실패 시 만료 key 미설정 | key 자체가 없으면 TTL 만료 이벤트가 발생하지 않아 `PaymentExpirationListener` 동작 불가. 실질적 fallback 없음 — 로그 경보 수준. stale PENDING 보정은 후속 이슈로 처리 |
| Step 4: 카카오 성공 → DB 실패 시 보상 취소도 실패 | 에러 로그 기록. outbox pattern은 별도 이슈 |
| Step 4: `@Transactional` 제거로 인한 기존 동작 변화 | `@DistributedLock`은 `readyPayment`에만 있어 영향 없음. `approvePayment`는 원래 락 없음 |
