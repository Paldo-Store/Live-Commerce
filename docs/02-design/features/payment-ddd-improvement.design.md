# Design: 결제 도메인 DDD 개선

**작성일**: 2026-04-18  
**대상 서비스**: `payment/`  
**참조 Plan**: `docs/01-plan/features/payment-ddd-improvement.plan.md`  
**브랜치**: `feature/PDSH-2-payment-ddd` (신규 생성 예정)

---

## 구현 순서

```
Step 1 → Step 2 → Step 3
각 Step은 독립 커밋. 이전 Step 컴파일 통과 후 다음 진행.
```

---

## Step 1: Repository 의존 방향 수정

### 문제

`PaymentQueryRepositoryImpl`이 `domain/repository/`에 있으나 JPAQueryFactory (JPA 구현체)를 사용 → infrastructure 책임이 domain 레이어에 노출.  
`PaymentQueryRepository` 인터페이스가 `application/dto/request/PaymentSearchCondition`을 참조 → domain이 application에 의존 (역방향).

### 파일 이동

| 현재 위치 | 이동 후 위치 | 변경 내용 |
|-----------|------------|----------|
| `application/dto/request/PaymentSearchCondition.java` | `domain/repository/PaymentSearchCondition.java` | package 변경 |
| `domain/repository/PaymentQueryRepositoryImpl.java` | `infrastructure/persistence/PaymentQueryRepositoryImpl.java` | package 변경 |

### package 변경 상세

**PaymentSearchCondition.java**
```java
// 변경 전
package com.live_commerce.payment.application.dto.request;

// 변경 후
package com.live_commerce.payment.domain.repository;
```

**PaymentQueryRepositoryImpl.java**
```java
// 변경 전
package com.live_commerce.payment.domain.repository;

// 변경 후
package com.live_commerce.payment.infrastructure.persistence;
```

### import 업데이트 대상 파일

| 파일 | 업데이트 내용 |
|------|------------|
| `domain/repository/PaymentQueryRepository.java` | `PaymentSearchCondition` import → `domain.repository` |
| `infrastructure/persistence/PaymentQueryRepositoryImpl.java` | `PaymentSearchCondition` import → `domain.repository` |
| `application/service/PaymentServiceV2.java` | `PaymentSearchCondition` import → `domain.repository` |
| `application/service/PaymentService.java` | `PaymentSearchCondition` import → `domain.repository` (있다면) |
| `presentation/controller/PaymentControllerV2.java` | `PaymentSearchCondition` import → `domain.repository` |

### QuerydslConfig 확인

`infrastructure/config/QuerydslConfig.java`의 `JPAQueryFactory` Bean 주입이  
`PaymentQueryRepositoryImpl`에 자동 연결되는지 확인 (Spring Bean 등록 방식 점검).

> 가정: `PaymentRepository`가 `PaymentQueryRepository`를 extend하고 있어 Spring Data JPA가 `PaymentQueryRepositoryImpl`을 자동 감지한다. 이동 후에도 동일 동작 보장을 위해 `@Repository` 명시 추가.

### 완료 조건

- `./gradlew :payment:compileJava` 통과
- `domain/` 하위에 JPAQueryFactory, QueryDSL 관련 import 없음

---

## Step 2: 상태 전이 도메인화

### 문제

`Payment.updateStatus(PaymentStatus)`는 어떤 상태에서도 어떤 상태로든 변경 가능.  
비즈니스 규칙(COMPLETED → PENDING 불가 등)이 application service 조건문에 분산되어 있음.

### 상태 전이 규칙

```
PENDING   → complete(tid) → COMPLETED
PENDING   → fail()        → FAILED
PENDING   → cancel()      → CANCELED
COMPLETED → refund()      → REFUND
FAILED    → (terminal, 변경 불가)
CANCELED  → (terminal, 변경 불가)
REFUND    → (terminal, 변경 불가)
```

### Payment.java 변경 사항

```java
// 추가: 상태 전이 메서드
public void complete(String tid) {
    validateTransition(PaymentStatus.COMPLETED);
    this.tid = tid;
    this.status = PaymentStatus.COMPLETED;
}

public void fail() {
    validateTransition(PaymentStatus.FAILED);
    this.status = PaymentStatus.FAILED;
}

public void cancel() {
    validateTransition(PaymentStatus.CANCELED);
    this.status = PaymentStatus.CANCELED;
}

public void refund() {
    validateTransition(PaymentStatus.REFUND);
    this.status = PaymentStatus.REFUND;
}

// 제거: updateStatus() — package-private으로 낮추거나 제거
// (V1 서비스 참조 여부 확인 후 결정)

private void validateTransition(PaymentStatus next) {
    if (!status.canTransitionTo(next)) {
        throw new IllegalStateException(
            "유효하지 않은 상태 전이: " + status + " → " + next
        );
    }
}
```

### PaymentStatus.java 변경 사항

```java
public enum PaymentStatus {
    PENDING, COMPLETED, FAILED, CANCELED, REFUND;

    public boolean canTransitionTo(PaymentStatus next) {
        return switch (this) {
            case PENDING   -> next == COMPLETED || next == FAILED || next == CANCELED;
            case COMPLETED -> next == REFUND;
            default        -> false; // FAILED, CANCELED, REFUND 는 terminal
        };
    }
}
```

### PaymentServiceV2.java 변경 사항

| 현재 코드 | 변경 후 |
|-----------|---------|
| `payment.updateStatus(PaymentStatus.FAILED)` (approvePayment) | `payment.fail()` |
| `payment.updateStatus(PaymentStatus.COMPLETED)` (approvePayment) | `payment.complete(approveDto.tid())` |
| `payment.updateStatus(PaymentStatus.REFUND)` (refundPaymentByOrderId) | `payment.refund()` |
| `payment.updateStatus(PaymentStatus.REFUND)` (compensateRefundByOrderId) | `payment.refund()` |
| `payment.updateStatus(PaymentStatus.CANCELED)` (cancelPaymentByOrderId) | `payment.cancel()` |

> 가정: `approveDto.tid()` 필드가 `KakaoPayApproveDto`에 존재한다. 없으면 `complete()` 파라미터 제거 후 `tid`는 `assignTid()`로 별도 설정.

### INVALID_STATUS 예외 처리 정리

기존 service의 `if (payment.getStatus() != PaymentStatus.PENDING)` 가드는 `Payment.fail()` 등이 `IllegalStateException`을 throw하므로 제거 가능.  
단, 기존 `CustomException(PaymentExceptionCode.INVALID_STATUS)`와 메시지 통일을 위해 `validateTransition`에서 `CustomException` 또는 도메인 전용 exception을 throw하도록 조정.

> 선택: `IllegalStateException`은 domain 레이어에서 허용 (Spring 미의존). Application service의 `@ExceptionHandler`에서 매핑 추가.

### updateStatus() 처리

`PaymentService` (V1)에서 `updateStatus()` 호출 여부 확인:
- 호출 없음 → 메서드 제거
- 호출 있음 → `@Deprecated` 마킹 후 V1 흐름 유지 (V1은 변경 금지 원칙)

### 완료 조건

- `Payment.updateStatus()` 직접 호출이 V2에서 사라짐
- 유효하지 않은 전이 시 예외 발생 (단위 검증: 코드 리뷰로 확인)
- `./gradlew :payment:compileJava` 통과

---

## Step 3: Domain Event 도입

### 문제

`PaymentServiceV2`가 `PaymentCompletedEvent`, `PaymentFailedEvent` (infrastructure 클래스)를 직접 생성하고 `PaymentEventProducer`(infrastructure Bean)를 직접 주입.  
→ Application service가 Kafka 구현 세부에 의존.

### 목표 흐름

```
PaymentServiceV2.approvePayment()
  └─ payment.complete(tid)                  ← domain 상태 전이
  └─ eventPublisher.publishEvent(           ← Spring ApplicationEventPublisher
       new PaymentCompletedDomainEvent(...)
     )
         ↓ (Spring Event, 동일 트랜잭션)
PaymentDomainEventListener (infrastructure)
  @TransactionalEventListener(phase = AFTER_COMMIT)
  └─ paymentEventProducer.sendPaymentCompleted(...)  ← Kafka 발행
```

### 신규 파일

**`domain/event/PaymentCompletedDomainEvent.java`**
```java
package com.live_commerce.payment.domain.event;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentCompletedDomainEvent(
    UUID orderId,
    BigDecimal amount
) {}
```

**`domain/event/PaymentFailedDomainEvent.java`**
```java
package com.live_commerce.payment.domain.event;

import java.util.UUID;

public record PaymentFailedDomainEvent(
    UUID orderId,
    String reason
) {}
```

**`infrastructure/kafka/listener/PaymentDomainEventListener.java`**
```java
package com.live_commerce.payment.infrastructure.kafka.listener;

import com.live_commerce.payment.domain.event.PaymentCompletedDomainEvent;
import com.live_commerce.payment.domain.event.PaymentFailedDomainEvent;
import com.live_commerce.payment.infrastructure.kafka.event.PaymentCompletedEvent;
import com.live_commerce.payment.infrastructure.kafka.event.PaymentFailedEvent;
import com.live_commerce.payment.infrastructure.kafka.producer.PaymentEventProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class PaymentDomainEventListener {

    private final PaymentEventProducer paymentEventProducer;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPaymentCompleted(PaymentCompletedDomainEvent event) {
        paymentEventProducer.sendPaymentCompleted(
            new PaymentCompletedEvent(event.orderId(), "결제 완료", event.amount())
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPaymentFailed(PaymentFailedDomainEvent event) {
        paymentEventProducer.sendPaymentFailed(
            new PaymentFailedEvent(event.orderId(), event.reason())
        );
    }
}
```

### PaymentServiceV2.java 변경 사항

```java
// 추가 의존성
private final ApplicationEventPublisher eventPublisher;

// approvePayment() 변경
// 제거: paymentEventProducer 직접 주입 및 호출
// 변경 전:
paymentEventProducer.sendPaymentCompleted(new PaymentCompletedEvent(...));
// 변경 후:
eventPublisher.publishEvent(new PaymentCompletedDomainEvent(payment.getOrderId(), payment.getAmount()));

// 변경 전:
paymentEventProducer.sendPaymentFailed(new PaymentFailedEvent(...));
// 변경 후:
eventPublisher.publishEvent(new PaymentFailedDomainEvent(payment.getOrderId(), "카카오페이 승인 실패"));
```

### 제거 대상

`PaymentServiceV2`의 `PaymentEventProducer` 필드 — `PaymentDomainEventListener`로 이동.

> 주의: `@TransactionalEventListener`의 기본 phase는 `AFTER_COMMIT`. Kafka 발행이 트랜잭션 커밋 후에 이루어지므로 DB와 Kafka 간 순서가 보장됨. 단, 발행 실패 시 재시도 없음 → 운영 환경에서는 outbox pattern 검토 필요 (현 단계 제외).

### 완료 조건

- `PaymentServiceV2`에서 `PaymentEventProducer` 직접 의존 제거
- `domain/event/` 하위에 Spring 어노테이션 없음
- Kafka 발행 경로: `eventPublisher → PaymentDomainEventListener → PaymentEventProducer`
- `./gradlew :payment:compileJava` 통과

---

## 전체 파일 변경 요약

### 이동/삭제

| 파일 | 변경 |
|------|------|
| `application/dto/request/PaymentSearchCondition.java` | → `domain/repository/PaymentSearchCondition.java` |
| `domain/repository/PaymentQueryRepositoryImpl.java` | → `infrastructure/persistence/PaymentQueryRepositoryImpl.java` |

### 수정

| 파일 | 수정 내용 |
|------|----------|
| `domain/model/Payment.java` | `complete()`, `fail()`, `cancel()`, `refund()`, `validateTransition()` 추가 |
| `domain/model/PaymentStatus.java` | `canTransitionTo()` 추가 |
| `domain/repository/PaymentQueryRepository.java` | `PaymentSearchCondition` import 경로 변경 |
| `application/service/PaymentServiceV2.java` | 상태 전이 메서드 호출로 교체, `eventPublisher` 추가, `paymentEventProducer` 제거 |
| `presentation/controller/PaymentControllerV2.java` | `PaymentSearchCondition` import 경로 변경 |

### 신규 생성

| 파일 | 설명 |
|------|------|
| `domain/event/PaymentCompletedDomainEvent.java` | 결제 완료 도메인 이벤트 |
| `domain/event/PaymentFailedDomainEvent.java` | 결제 실패 도메인 이벤트 |
| `infrastructure/kafka/listener/PaymentDomainEventListener.java` | 도메인 이벤트 → Kafka 브릿지 |
| `infrastructure/persistence/PaymentQueryRepositoryImpl.java` | 이동 후 신규 위치 |

---

## 리스크 및 대응

| 리스크 | 대응 |
|--------|------|
| `PaymentService` (V1)가 `updateStatus()` 호출 | Step 2 전에 V1 코드 확인. 호출 있으면 `@Deprecated` 유지 |
| `approveDto.tid()` 필드 부재 | `KakaoPayApproveDto` 확인 후 `complete()` 시그니처 조정 |
| `AFTER_COMMIT` 후 Kafka 실패 | 로그 경보 추가, outbox는 별도 이슈 |
| Spring Data JPA `PaymentQueryRepositoryImpl` 자동 감지 실패 | `@Repository` 추가 또는 `QuerydslConfig`에서 명시 Bean 등록 |
