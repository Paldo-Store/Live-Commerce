# Gap Analysis: payment-ddd-improvement

**분석 일시**: 2026-04-18  
**Design 문서**: `docs/02-design/features/payment-ddd-improvement.design.md`  
**구현 경로**: `payment/src/main/java/com/live_commerce/payment/`

---

## Match Rate

```
┌─────────────────────────────────────────────┐
│  Overall Match Rate: 100%                   │
├─────────────────────────────────────────────┤
│  Step 1 (30%) Repository 의존 방향:  30/30 ✓ │
│  Step 2 (35%) 상태 전이 도메인화:    35/35 ✓ │
│  Step 3 (35%) Domain Event 도입:    35/35 ✓ │
└─────────────────────────────────────────────┘
```

**판정: PASS** (기준 90% 이상)

---

## Step별 체크 결과

### Step 1: Repository 의존 방향 수정 — ✓ 완료

| 항목 | 결과 |
|------|:----:|
| `PaymentSearchCondition` → `domain/repository/` 이동 | ✓ |
| `PaymentQueryRepositoryImpl` → `infrastructure/persistence/` 이동 + `@Repository` | ✓ |
| 구 `application/dto/request/PaymentSearchCondition.java` 제거 | ✓ |
| 구 `domain/repository/PaymentQueryRepositoryImpl.java` 제거 | ✓ |
| `PaymentQueryRepository` import 업데이트 | ✓ |
| `PaymentServiceV2` import 업데이트 | ✓ |
| `PaymentService` (V1) import 업데이트 | ✓ |
| `PaymentControllerV2` import 업데이트 | ✓ |
| `PaymentController` (V1) import 업데이트 | ✓ |
| `domain/` 하위 JPAQueryFactory/QueryDSL import 0건 | ✓ |

### Step 2: 상태 전이 도메인화 — ✓ 완료

| 항목 | 결과 |
|------|:----:|
| `PaymentStatus.canTransitionTo()` — 전이 규칙 switch 구현 | ✓ |
| `Payment.complete()` 추가 | ✓ |
| `Payment.fail()` 추가 | ✓ |
| `Payment.cancel()` 추가 | ✓ |
| `Payment.refund()` 추가 | ✓ |
| `Payment.validateTransition()` — 위반 시 `IllegalStateException` | ✓ |
| `Payment.updateStatus()` `@Deprecated` 마킹 | ✓ |
| V2 `approvePayment` fail 경로 → `payment.fail()` | ✓ |
| V2 `approvePayment` complete 경로 → `payment.complete()` | ✓ |
| V2 `refundPaymentByOrderId` → `payment.refund()` | ✓ |
| V2 `compensateRefundByOrderId` → `payment.refund()` | ✓ |
| V2 `cancelPaymentByOrderId` → `payment.cancel()` | ✓ |
| V2 내 `updateStatus()` 직접 호출 0건 | ✓ |

### Step 3: Domain Event 도입 — ✓ 완료

| 항목 | 결과 |
|------|:----:|
| `domain/event/PaymentCompletedDomainEvent` — 순수 record, Spring 의존 없음 | ✓ |
| `domain/event/PaymentFailedDomainEvent` — 순수 record, Spring 의존 없음 | ✓ |
| `PaymentDomainEventListener` — `@TransactionalEventListener(AFTER_COMMIT)` × 2 | ✓ |
| 리스너 → `PaymentEventProducer` 생성자 주입 | ✓ |
| 리스너 → 기존 Kafka 이벤트 객체로 매핑 발행 | ✓ |
| `PaymentServiceV2` → `ApplicationEventPublisher` 주입 | ✓ |
| `PaymentServiceV2` 내 `PaymentEventProducer` 직접 의존 0건 | ✓ |
| Kafka 발행 경로: `eventPublisher → Listener → Producer → Kafka` | ✓ |

---

## 설계 허용 범위 내 변형 (Gap 아님)

| 항목 | 설계 명시 | 실제 구현 | 평가 |
|------|-----------|-----------|------|
| `Payment.complete()` 시그니처 | `complete(String tid)` 또는 파라미터 없음 (fallback 허용) | `complete()` — tid는 `readyPayment`에서 `assignTid()`로 이미 할당 | 설계 허용 경로 |
| `INVALID_STATUS` 사전 가드 유지 | 제거 가능, 단 선택 | V2 `approvePayment`에 가드 유지 (이중 방어) | 설계 허용 경로 |
| V1 `updateStatus()` 잔존 | V1 호출 있으면 `@Deprecated` 유지 | V1 `PaymentService` + `PaymentExpirationListener`에서 호출, `@Deprecated` 적용 | V1 불변 원칙과 일치 |

---

## 관찰 포인트 (비차단, 후속 작업 권장)

| 구분 | 내용 |
|------|------|
| 🟡 outbox 미구현 | `AFTER_COMMIT` 후 Kafka 발행 실패 시 재시도 없음. 설계 범위 외 — 별도 이슈로 추적 필요 |

---

## 결론

Design 문서의 3개 Step 필수 항목을 **모두 충족**. Match Rate **100%**, 90% 기준 통과.

- `domain/` 레이어에서 JPA/QueryDSL 의존이 완전히 제거됨
- 상태 전이 규칙이 도메인 모델에 캡슐화됨
- Kafka 발행이 도메인 이벤트 → 인프라 리스너 경로로 분리됨
