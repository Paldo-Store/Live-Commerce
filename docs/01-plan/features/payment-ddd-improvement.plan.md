# Plan: 결제 도메인 DDD 개선

**작성일**: 2026-04-17  
**대상 서비스**: `client/payment`  
**우선순위**: 중 — 기능 동작에는 이상 없으나 도메인 모델 순도 저하

---

## 1. 배경 및 목적

PDSH-1 리팩토링(버그 수정·Dead Code 제거·권한 통합) 완료 후, 남은 DDD 구조 문제를 개선한다.  
현재 결제 도메인은 DDD 레이어를 표방하지만 세 가지 핵심 위반이 있다:

1. `PaymentQueryRepositoryImpl`이 `domain/` 아래에 있으나 JPA 구현체 — infrastructure로 이동 필요
2. `Payment.updateStatus()`에 상태 전이 규칙이 없어 임의 상태 변경 허용
3. Kafka 이벤트가 application service에서 직접 생성·발행되어 도메인 의도가 숨어있음

> **제외 범위**: V1/V2 병존 구조는 학습 목적으로 유지. 변경하지 않는다.

---

## 2. 발견된 문제

### R1 — Repository 레이어 위반

| 위치 | 문제 |
|------|------|
| `domain/repository/PaymentQueryRepositoryImpl.java` | `JPAQueryFactory` + QueryDSL — JPA 구현체가 domain에 위치 |
| `application/dto/request/PaymentSearchCondition.java` | 검색 조건 DTO가 application에 있으나 domain/repository 인터페이스가 참조 → 의존 방향 위반 |

**목표 구조**:
```
domain/
  repository/
    PaymentQueryRepository.java (인터페이스)  ← PaymentSearchCondition도 여기로
infrastructure/
  persistence/
    PaymentQueryRepositoryImpl.java          ← 구현체 이동
```

### R2 — 상태 전이 규칙 미적용

| 위치 | 문제 |
|------|------|
| `domain/model/Payment.java` `updateStatus()` | 어떤 상태에서 어떤 상태로도 전이 가능. 유효하지 않은 전이(COMPLETED→PENDING 등) 차단 없음 |

**목표 전이 규칙**:
```
PENDING   → COMPLETED, FAILED, CANCELED
COMPLETED → REFUND
FAILED    → (불변)
CANCELED  → (불변)
REFUND    → (불변)
```

**목표 API**:
```java
payment.complete(String tid);   // PENDING → COMPLETED
payment.fail();                 // PENDING → FAILED
payment.cancel();               // PENDING → CANCELED
payment.refund();               // COMPLETED → REFUND
```

### R3 — Domain Event 부재

| 위치 | 문제 |
|------|------|
| `PaymentServiceV2.approvePayment()` | `PaymentCompletedEvent` (infrastructure 클래스)를 직접 생성·발행. 도메인 의도가 서비스 코드에 노출 |
| `infrastructure/kafka/event/PaymentCompletedEvent.java` | Kafka 메시지 구조체가 도메인 이벤트를 대체하는 구조 |

**목표 구조**:
```
domain/event/
  PaymentCompletedEvent.java   (순수 도메인 이벤트 — Spring 의존 없음)
  PaymentRefundedEvent.java

infrastructure/kafka/
  PaymentEventListener.java    (@TransactionalEventListener)
  → 도메인 이벤트 수신 후 Kafka 발행
```

---

## 3. 개선 단계

### Step 1: Repository 의존 방향 수정
- `PaymentSearchCondition`을 `domain/repository/` 로 이동
- `PaymentQueryRepositoryImpl`을 `infrastructure/persistence/` 로 이동
- 기존 인터페이스(`domain/repository/PaymentQueryRepository`) 유지
- **로직 변경 없음** — 순수 파일 이동

### Step 2: 상태 전이 도메인화
- `Payment` 모델에 `complete()`, `fail()`, `cancel()`, `refund()` 추가
- 불가능한 전이 시 `PaymentStatusException` (domain exception) throw
- `updateStatus()` 직접 호출 제거 — 모든 상태 변경은 명시적 메서드 경유
- `compensateRefundByOrderId` 흐름과 호환성 확인 필수

### Step 3: Domain Event 도입
- `domain/event/PaymentCompletedEvent`, `PaymentRefundedEvent` (POJO) 생성
- `Payment.complete()` 내부에서 `ApplicationEventPublisher` 없이 이벤트 수집 (도메인은 Spring 의존 금지)
- Application service에서 `ApplicationEventPublisher.publishEvent()` 호출
- `infrastructure/kafka/PaymentEventListener` 에서 `@TransactionalEventListener` 수신 후 Kafka 발행
- 기존 `PaymentEventProducer` 직접 호출 제거

---

## 4. 리스크

| 리스크 | 수준 | 대응 |
|--------|------|------|
| Step 2: `compensateRefundByOrderId` 보상 흐름과 상태 충돌 | 중 | 구현 전 해당 메서드 흐름 코드 리뷰 필수 |
| Step 3: `@TransactionalEventListener` 기본값은 `AFTER_COMMIT` — Kafka 발행 타이밍 변경 | 중 | phase 명시(`AFTER_COMMIT`) + 실패 시 로그 경보 추가 |
| PaymentServiceV1이 `updateStatus()` 직접 호출 중일 경우 | 낮 | Step 2 전에 V1 참조 코드 확인 |

---

## 5. 완료 조건

- [ ] Step 1: `PaymentQueryRepositoryImpl` + `PaymentSearchCondition` 이동 후 컴파일 통과
- [ ] Step 2: 상태 전이 메서드 추가, 허용되지 않는 전이 시 예외 발생 확인
- [ ] Step 3: 결제 완료 시 Kafka 이벤트 발행 경로가 domain event → listener → Kafka 로 흐름
- [ ] drift-check.sh 에서 레이어 위반 0건
- [ ] 기존 V2 흐름 정상 동작 (health.sh + log.sh 확인)

---

## 6. 다음 단계

`/pdca design payment-ddd-improvement` 로 각 Step의 파일·클래스 변경 세부 설계 진행
