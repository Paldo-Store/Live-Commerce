# Design: payment Kafka 이벤트 유실 방지를 위한 Outbox Pattern 도입

**작성일**: 2026-04-28  
**대상 서비스**: `payment/`  
**참조 Plan**: `docs/01-plan/features/payment-outbox-pattern.plan.md`

---

## 구현 순서

```
Step 1 (O1-a) Outbox 도메인 모델 + 릴레이 인프라
Step 2 (O1-b) PaymentTxProcessor / EventListener 전환
Step 3 (O1-c) 이벤트 리스너 / 도메인 이벤트 정리
Step 4 (O2)   Payment.expiresAt + 만료 배치 스케줄러
각 Step은 독립 커밋. 이전 Step 컴파일 통과 후 다음 진행.
```

---

## 현재 구조 (변경 전)

```
PaymentTxProcessor.complete() / fail()  [REQUIRES_NEW tx]
  └─ eventPublisher.publishEvent(PaymentCompletedDomainEvent)
     └─ @TransactionalEventListener(AFTER_COMMIT)
        └─ PaymentDomainEventListener.onPaymentCompleted()
           └─ kafkaTemplate.send("payment-completed", ...)   ← 유실 지점
```

DB 커밋과 Kafka publish 사이 JVM 크래시 → 이벤트 영구 유실, 재시도 없음.

---

## Step 1: O1-a — Outbox 도메인 모델 + 릴레이 인프라

### 신규 파일

#### `domain/model/OutboxStatus.java`
```java
package com.live_commerce.payment.domain.model;

public enum OutboxStatus {
    PENDING, PUBLISHED, FAILED
}
```

#### `domain/model/PaymentOutbox.java`
```java
@Entity @Getter
@Table(name = "p_payment_outbox")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentOutbox {

    @Id @UuidGenerator
    private UUID id;

    @Column(nullable = false)
    private UUID orderId;

    @Column(nullable = false)
    private String eventType;     // "PAYMENT_COMPLETED" | "PAYMENT_FAILED"

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;       // JSON

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OutboxStatus status;

    @CreatedDate
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime publishedAt;

    @Column(nullable = false)
    private int retryCount = 0;

    public static PaymentOutbox ofCompleted(UUID orderId, BigDecimal amount) {
        return new PaymentOutbox(orderId, "PAYMENT_COMPLETED",
            "{\"orderId\":\"" + orderId + "\",\"amount\":\"" + amount.toPlainString() + "\"}");
    }

    public static PaymentOutbox ofFailed(UUID orderId, String reason) {
        return new PaymentOutbox(orderId, "PAYMENT_FAILED",
            "{\"orderId\":\"" + orderId + "\",\"reason\":\"" + reason + "\"}");
    }

    private PaymentOutbox(UUID orderId, String eventType, String payload) {
        this.orderId = orderId;
        this.eventType = eventType;
        this.payload = payload;
        this.status = OutboxStatus.PENDING;
    }

    public void markPublished() {
        this.status = OutboxStatus.PUBLISHED;
        this.publishedAt = LocalDateTime.now();
    }

    public void markFailed() {
        this.status = OutboxStatus.FAILED;
    }

    public void incrementRetry() {
        this.retryCount++;
    }
}
```

`BaseEntity` 미상속 이유: `deletedStatus` 등 감사 필드가 outbox 레코드에 불필요. `createdAt`만 직접 선언.

#### `domain/repository/PaymentOutboxRepository.java`
```java
public interface PaymentOutboxRepository extends JpaRepository<PaymentOutbox, UUID> {
    List<PaymentOutbox> findTop50ByStatusOrderByCreatedAt(OutboxStatus status);
}
```

### DB 마이그레이션 (`schema.sql` 또는 별도 migration 스크립트)

```sql
CREATE TABLE p_payment_outbox (
    id            UUID PRIMARY KEY,
    order_id      UUID         NOT NULL,
    event_type    VARCHAR(50)  NOT NULL,
    payload       TEXT         NOT NULL,
    status        VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
    published_at  TIMESTAMP,
    retry_count   INT          NOT NULL DEFAULT 0
);

CREATE INDEX idx_outbox_status_created ON p_payment_outbox (status, created_at);
```

### 완료 조건

- 엔티티 컴파일 통과
- `PaymentOutboxRepository` Bean 등록 확인

---

## Step 2: O1-b — PaymentTxProcessor 전환

### `application/service/PaymentTxProcessor.java` 변경

**추가 필드**
```java
private final PaymentOutboxRepository paymentOutboxRepository;
private final ObjectMapper objectMapper;  // Spring Boot auto-configure Bean
```

**`complete()` 변경**
```java
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void complete(UUID orderId) {
    Payment p = findByOrderId(orderId);
    p.complete();
    paymentOutboxRepository.save(PaymentOutbox.ofCompleted(p.getOrderId(), p.getAmount()));
    // eventPublisher 호출 제거
}
```

**`fail()` 변경**
```java
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void fail(UUID orderId, String reason) {
    Payment p = findByOrderId(orderId);
    p.fail();
    paymentOutboxRepository.save(PaymentOutbox.ofFailed(p.getOrderId(), reason));
    // eventPublisher 호출 제거
}
```

`refund()`, `cancel()` — 주문 서비스 알림이 Feign(`orderClient`)을 직접 호출하므로 변경 없음.

### `ApplicationEventPublisher` 필드 제거

`complete()`, `fail()`에서 더 이상 이벤트를 publish하지 않으면 `eventPublisher` 필드도 제거 가능. `readyPayment`의 `PaymentReadyDomainEvent`는 `PaymentServiceV2`가 publish하므로 `PaymentTxProcessor`의 `eventPublisher` 의존성은 완전히 제거된다.

### 완료 조건

- `PaymentTxProcessor`에서 `ApplicationEventPublisher` 의존성 없음
- `complete()` / `fail()` 호출 시 `p_payment_outbox` 레코드 삽입 확인

---

## Step 3: O1-c — 릴레이 스케줄러 + 리스너 정리

### 신규 파일

#### `infrastructure/scheduler/OutboxRelayScheduler.java`
```java
@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxRelayScheduler {

    private final PaymentOutboxRepository outboxRepository;
    private final PaymentEventProducer paymentEventProducer;
    private final ObjectMapper objectMapper;

    private static final int MAX_RETRY = 3;

    @DistributedLock(key = "'outbox:relay'")
    @Scheduled(fixedDelay = 3000)
    public void relay() {
        List<PaymentOutbox> pending =
            outboxRepository.findTop50ByStatusOrderByCreatedAt(OutboxStatus.PENDING);

        for (PaymentOutbox outbox : pending) {
            try {
                publish(outbox);
                outbox.markPublished();
            } catch (Exception e) {
                outbox.incrementRetry();
                if (outbox.getRetryCount() >= MAX_RETRY) {
                    outbox.markFailed();
                    log.error("[Outbox] 최대 재시도 초과 - 수동 처리 필요: id={}, orderId={}", outbox.getId(), outbox.getOrderId(), e);
                } else {
                    log.warn("[Outbox] 발행 실패 재시도 예정: id={}, retry={}", outbox.getId(), outbox.getRetryCount(), e);
                }
            }
            outboxRepository.save(outbox);
        }
    }

    @SuppressWarnings("unchecked")
    private void publish(PaymentOutbox outbox) throws Exception {
        Map<String, Object> map = objectMapper.readValue(outbox.getPayload(), Map.class);
        UUID orderId = UUID.fromString((String) map.get("orderId"));

        switch (outbox.getEventType()) {
            case "PAYMENT_COMPLETED" -> {
                BigDecimal amount = new BigDecimal((String) map.get("amount"));
                paymentEventProducer.sendPaymentCompleted(
                    new PaymentCompletedEvent(orderId, "결제 완료", amount)
                );
            }
            case "PAYMENT_FAILED" -> {
                String reason = (String) map.get("reason");
                paymentEventProducer.sendPaymentFailed(
                    new PaymentFailedEvent(orderId, reason)
                );
            }
            default -> throw new IllegalArgumentException("알 수 없는 eventType: " + outbox.getEventType());
        }
    }
}
```

`@EnableScheduling`을 `PaymentApplication.java`에 추가한다.

### `infrastructure/kafka/listener/PaymentDomainEventListener.java` 변경

Kafka 발행 핸들러 2개 제거. Redis 핸들러만 유지.

```java
// 제거
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
public void onPaymentCompleted(PaymentCompletedDomainEvent event) { ... }

@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
public void onPaymentFailed(PaymentFailedDomainEvent event) { ... }

// 유지
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
public void onPaymentReady(PaymentReadyDomainEvent event) { ... }
```

`paymentEventProducer`, `retryTemplate` 필드 제거. `redissonClient`, `paymentExpireMinutes` 필드만 유지.

### 삭제할 파일

| 파일 | 이유 |
|------|------|
| `domain/event/PaymentCompletedDomainEvent.java` | Outbox로 대체, 리스너에서 더 이상 사용 안 함 |
| `domain/event/PaymentFailedDomainEvent.java` | 동일 |

`PaymentReadyDomainEvent`는 Redis 키 설정 경로에 여전히 사용되므로 유지.

### `infrastructure/listener/PaymentExpirationListener.java` 변경

현재 `payment.fail()` 직접 호출 → `paymentTxProcessor.fail()` 로 교체. Outbox에 기록되어 Kafka 발행 경로 통일.

```java
// 변경 전
payment.fail();

// 변경 후
paymentTxProcessor.fail(payment.getOrderId(), "결제 유효시간 만료");
```

`PaymentTxProcessor` 필드 추가 주입 필요.

### `infrastructure/kafka/producer/PaymentEventProducer.java` 변경

`send()` 내 `log.info` 제거 (성공 경로 로그 금지 원칙).

### 완료 조건

- `OutboxRelayScheduler` 3초 주기 실행, Kafka 정상 전송 확인
- 앱 재기동 후 PENDING 레코드 재발행 확인
- `PaymentDomainEventListener`에 `PaymentEventProducer` 의존성 없음

---

## Step 4: O2 — Payment.expiresAt + 만료 배치

### `domain/model/Payment.java` 변경

```java
// 추가 필드
private LocalDateTime expiresAt;

// 추가 메서드
public void expireAt(LocalDateTime expiresAt) {
    this.expiresAt = expiresAt;
}
```

### `application/service/PaymentServiceV2.java` 변경

`readyPayment()` — `paymentRepository.save(payment)` 이전에 만료 시각 설정.

```java
// 추가 필드
@Value("${payment.expire-minutes:10}")
private int paymentExpireMinutes;

// readyPayment() 내 추가
payment.expireAt(LocalDateTime.now().plusMinutes(paymentExpireMinutes));
```

### `domain/repository/PaymentRepository.java` 변경

```java
// 추가
List<Payment> findByStatusAndExpiresAtBefore(PaymentStatus status, LocalDateTime threshold);
```

### 신규 파일

#### `infrastructure/scheduler/PaymentExpiredBatchScheduler.java`
```java
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentExpiredBatchScheduler {

    private final PaymentRepository paymentRepository;
    private final PaymentTxProcessor paymentTxProcessor;

    @Scheduled(fixedDelay = 60_000)
    public void expireOverdue() {
        List<Payment> expired = paymentRepository
            .findByStatusAndExpiresAtBefore(PaymentStatus.PENDING, LocalDateTime.now());

        for (Payment payment : expired) {
            try {
                paymentTxProcessor.fail(payment.getOrderId(), "결제 유효시간 만료(배치)");
            } catch (Exception e) {
                log.error("[Batch] 만료 처리 실패: orderId={}", payment.getOrderId(), e);
            }
        }
    }
}
```

`paymentTxProcessor.fail()`은 멱등 설계 (`status == PENDING`인 경우만 `PaymentStatus.FAILED`로 전이, 그 외 `validateTransition` 에서 예외 → catch로 흡수).

### DB 마이그레이션

```sql
ALTER TABLE p_payment ADD COLUMN expires_at TIMESTAMP;
CREATE INDEX idx_payment_expires ON p_payment (status, expires_at);
```

### 완료 조건

- Redis key 미설정 PENDING 결제가 1분 배치 후 FAILED 처리
- `p_payment_outbox`에 PAYMENT_FAILED 레코드 삽입 확인
- 이미 FAILED 처리된 결제에 배치 중복 실행 시 `IllegalStateException` 로그 없음

---

## 전체 파일 변경 요약

### 신규 생성

| 파일 | 설명 |
|------|------|
| `domain/model/OutboxStatus.java` | 릴레이 상태 enum |
| `domain/model/PaymentOutbox.java` | Outbox 엔티티 |
| `domain/repository/PaymentOutboxRepository.java` | JPA 인터페이스 |
| `infrastructure/scheduler/OutboxRelayScheduler.java` | Kafka 릴레이 스케줄러 |
| `infrastructure/scheduler/PaymentExpiredBatchScheduler.java` | 만료 결제 보정 배치 |

### 수정

| 파일 | 변경 내용 |
|------|----------|
| `application/service/PaymentTxProcessor.java` | outbox 저장, `eventPublisher` 제거 |
| `application/service/PaymentServiceV2.java` | `payment.expireAt()` 호출 |
| `domain/model/Payment.java` | `expiresAt` 필드 + `expireAt()` 메서드 |
| `domain/repository/PaymentRepository.java` | `findByStatusAndExpiresAtBefore()` 추가 |
| `infrastructure/kafka/listener/PaymentDomainEventListener.java` | Kafka 핸들러 2개 제거 |
| `infrastructure/listener/PaymentExpirationListener.java` | `paymentTxProcessor.fail()` 교체 |
| `infrastructure/kafka/producer/PaymentEventProducer.java` | `log.info` 제거 |
| `PaymentApplication.java` | `@EnableScheduling` 추가 |

### 삭제

| 파일 | 이유 |
|------|------|
| `domain/event/PaymentCompletedDomainEvent.java` | Outbox로 대체 |
| `domain/event/PaymentFailedDomainEvent.java` | 동일 |

---

## 리스크 및 대응

| 리스크 | 대응 |
|--------|------|
| 다중 인스턴스 릴레이 중복 실행 | `@DistributedLock(key = "'outbox:relay'")` 으로 단일 인스턴스 실행 보장 |
| Kafka send 후 `markPublished()` 저장 실패 | 재시도 시 Kafka 중복 전송 가능 → Consumer 멱등성으로 처리(수신 서비스 책임) |
| `PaymentOutbox.ofCompleted/ofFailed` 내 JSON 수동 생성 | 특수문자 포함 reason 시 JSON 깨짐 → `ObjectMapper.writeValueAsString()` 사용으로 교체 권장 (Step 1 구현 시 결정) |
| `p_payment.expires_at` NOT NULL 이슈 | NULL 허용으로 마이그레이션. 기존 레코드는 NULL → 배치가 처리 안 함(의도된 동작) |

---

## 알려진 한계

| 항목 | 내용 |
|------|------|
| 릴레이 지연 | 최대 3초 지연. Kafka 다운 시간에 따라 재시도 소진 후 FAILED → 수동 처리 필요 |
| FAILED outbox 보정 | `status = FAILED` 레코드는 자동 재발행 없음. 운영 쿼리 또는 별도 도구 필요 |
| Consumer 멱등성 | 릴레이 재시도로 인한 중복 메시지 처리는 주문 서비스 책임 (설계 외 범위) |
