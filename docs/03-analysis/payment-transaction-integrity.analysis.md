# Gap Analysis: payment-transaction-integrity

**분석 일시**: 2026-04-20 (후속 정리 반영: 2026-04-20)  
**Design 문서**: `docs/02-design/features/payment-transaction-integrity.design.md`  
**구현 경로**: `payment/src/main/java/com/live_commerce/payment/`

---

## Match Rate

```
┌──────────────────────────────────────────────┐
│  Overall Match Rate: 100%                    │
├──────────────────────────────────────────────┤
│  Step 1 (25%) T4 상수화:           25/25 ✓   │
│  Step 2 (25%) T1 Redis 만료 검증:  25/25 ✓   │
│  Step 3 (25%) T3 readyPayment 분리: 25/25 ✓  │
│  Step 4 (25%) T2 트랜잭션 분리:    25/25 ✓   │
└──────────────────────────────────────────────┘
```

**판정: PASS** (기준 90% 이상)

---

## Step별 체크 결과

### Step 1: T4 상수화 — ✓ 완료

| 항목 | 결과 |
|------|:----:|
| `application/port/PaymentCachePort.java` 인터페이스 존재 | ✓ |
| `infrastructure/redis/PaymentCacheAdapter.java` 구현체 존재 (`EXPIRE_KEY_PREFIX` 캡슐화) | ✓ |
| `application.yml` `payment.expire-minutes: 10` 추가 (`PaymentCacheAdapter` 단독 소유) | ✓ |
| `PaymentServiceV2` 로컬 상수 제거, `PaymentCachePort` 위임 | ✓ |
| `PaymentExpirationListener` `PaymentCachePort` 위임 | ✓ |

### Step 2: T1 Redis 만료 검증 — ✓ 완료

| 항목 | 결과 |
|------|:----:|
| `PAYMENT_EXPIRED` (HTTP 410) `PaymentExceptionCode` 추가 | ✓ |
| `approvePayment` Redis key 존재 검증 + `PAYMENT_EXPIRED` throw | ✓ |
| 승인 완료 후 `paymentCachePort.deleteExpiry()` 호출 | ✓ |

### Step 3: T3 readyPayment Redis 분리 — ✓ 완료

| 항목 | 결과 |
|------|:----:|
| `PaymentReadyDomainEvent` 순수 record (Spring 의존 없음) | ✓ |
| `readyPayment` 내 `bucket.set()` 직접 호출 제거 | ✓ |
| `readyPayment` 내 `PaymentReadyDomainEvent` publish | ✓ |
| `PaymentDomainEventListener` `PaymentCachePort` 주입 (`RedissonClient`·`@Value` 제거) | ✓ |
| `onPaymentReady` `@TransactionalEventListener(AFTER_COMMIT)` 핸들러 | ✓ |

### Step 4: T2 트랜잭션 분리 — ✓ 완료

| 항목 | 결과 |
|------|:----:|
| `TransactionTemplate` 주입 (Spring Boot auto-config 활용, 별도 `@Configuration` 없음) | ✓ |
| `approvePayment` `@Transactional` 제거 | ✓ |
| `TransactionTemplate` 필드 주입 | ✓ |
| Fail 경로 `transactionTemplate.executeWithoutResult` 적용 | ✓ |
| Complete 경로 `transactionTemplate.executeWithoutResult` 적용 | ✓ |
| 카카오 성공 → DB 실패 보상 취소 + 에러 로그 | ✓ |

---

## 발견된 Gap

없음.

---

## 관찰 포인트 (비차단, 차기 이슈)

| 항목 | 내용 |
|------|------|
| `refundPaymentByOrderId`, `compensateRefundByOrderId` | 카카오 API 호출이 여전히 `@Transactional` 내 존재 — 동일 패턴 적용 검토 필요 |

## 후속 정리 이력 (코드 분석 후 적용)

| 항목 | 파일 | 내용 |
|------|------|------|
| 데드 필드 제거 | `PaymentServiceV2` | `@Value("${payment.expire-minutes:10}") paymentExpireMinutes` — 주입했으나 실제 사용처 없음. `PaymentDomainEventListener`만 해당 값 사용 |
| 불필요 Bean 제거 | `TransactionConfig.java` (삭제) | `TransactionTemplate` Bean 직접 등록 → Spring Boot `TransactionAutoConfiguration`이 자동 제공하므로 불필요 |
| 불필요 어노테이션 제거 | `PaymentQueryRepositoryImpl` | `@Repository` — Spring Data JPA가 네이밍 컨벤션(`*Impl`)으로 자동 감지하므로 불필요 |
| 상수 중복 제거 | `PaymentService` (V1) | 로컬 `PAYMENT_EXPIRE_KEY_PREFIX = "payment:expire:"` → `PaymentCachePort.setExpiry()` 위임으로 제거 |
| 무의미한 어노테이션 제거 | `Payment.updateStatus()` | `@Deprecated` — V1이 여전히 호출하므로 어노테이션이 의미 없음. 어노테이션 제거, V2 마이그레이션 완료 후 메서드 삭제 예정 |
| 레이어 의존 방향 개선 | `PaymentCachePort` / `PaymentCacheAdapter` | `application/` → `infrastructure/` 직접 참조 제거. Port 인터페이스로 역전 — `application/`은 Port만 알고 Redis 구현 몰라도 됨 |

---

## 알려진 한계

| 항목 | 내용 |
|------|------|
| Redis AFTER_COMMIT 저장 실패 시 fallback 없음 | `onPaymentReady`에서 Redis key 설정이 실패하면 TTL 만료 이벤트 자체가 발생하지 않아 `PaymentExpirationListener`가 동작할 기회가 없음. 문서에 "fallback 유지"로 표현했으나 실제로는 로그 경보만 존재하는 상태. stale PENDING 보정은 별도 보완 필요 (재시도, DB `expiresAt` 컬럼, 배치 등) |
| `TransactionTemplate` 전파 방식 | 기본 전파(`REQUIRED`)이므로 상위 트랜잭션이 존재하면 신규 생성이 아닌 참여가 됨. 현재 `approvePayment`에 `@Transactional`이 없어 실질적으로는 신규 생성되나, 구조적으로 `REQUIRES_NEW` 보장은 아님 |

---

## 결론

4개 Step 필수 항목 모두 구현에 반영. Match Rate **100%**, 기준 통과.

- Redis key와 만료 시간이 상수·설정으로 일원화됨
- 만료된 결제 승인 차단 (HTTP 410)
- DB 커밋 후에만 Redis key 설정 (트랜잭션 정합성 보장)
- 카카오 API 호출 구간에서 DB 커넥션 미점유, 보상 로직 추가
