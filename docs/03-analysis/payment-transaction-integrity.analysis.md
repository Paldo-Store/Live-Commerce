# Gap Analysis: payment-transaction-integrity

**분석 일시**: 2026-04-20 (후속 정리 반영: 2026-04-23)  
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
| `infrastructure/redis/PaymentRedisKeys.java` `EXPIRE_KEY_PREFIX` 상수 존재 | ✓ |
| `application.yml` `payment.expire-minutes: 10` 추가 | ✓ |
| `PaymentServiceV2` 로컬 하드코딩 제거, `PaymentRedisKeys.EXPIRE_KEY_PREFIX` 참조 | ✓ |
| `PaymentExpirationListener` `PaymentRedisKeys.EXPIRE_KEY_PREFIX` 참조 | ✓ |
| `PaymentDomainEventListener` `@Value("${payment.expire-minutes:10}")` 단독 소유 | ✓ |

### Step 2: T1 Redis 만료 검증 — ✓ 완료

| 항목 | 결과 |
|------|:----:|
| `PAYMENT_EXPIRED` (HTTP 410) `PaymentExceptionCode` 추가 | ✓ |
| `approvePayment` Redis key 존재 검증 + `PAYMENT_EXPIRED` throw | ✓ |
| 승인 완료 후 `expireBucket.delete()` 호출 | ✓ |

### Step 3: T3 readyPayment Redis 분리 — ✓ 완료

| 항목 | 결과 |
|------|:----:|
| `PaymentReadyDomainEvent` 순수 record (Spring 의존 없음) | ✓ |
| `readyPayment` 내 `bucket.set()` 직접 호출 제거 | ✓ |
| `readyPayment` 내 `PaymentReadyDomainEvent` publish | ✓ |
| `onPaymentReady` `@TransactionalEventListener(AFTER_COMMIT)` 핸들러 | ✓ |
| Redis set 실패 시 `RetryTemplate` 3회 재시도 + error log | ✓ |

### Step 4: T2 트랜잭션 분리 — ✓ 완료

| 항목 | 결과 |
|------|:----:|
| `PaymentTxProcessor` `@Transactional(REQUIRES_NEW)` Bean 생성 | ✓ |
| `approvePayment` `@Transactional` 제거 | ✓ |
| `PaymentTxProcessor` 필드 주입 | ✓ |
| Fail 경로 `paymentTxProcessor.fail()` 적용 | ✓ |
| Complete 경로 `paymentTxProcessor.complete()` 적용 | ✓ |
| 카카오 성공 → DB 실패 보상 취소 + 에러 로그 | ✓ |

---

## 발견된 Gap

없음.

---

## 관찰 포인트 (비차단, 차기 이슈)

| 항목 | 내용 |
|------|------|
| `refundPaymentByOrderId`, `compensateRefundByOrderId` | ~~카카오 API 호출이 여전히 `@Transactional` 내 존재~~ → **해결됨** (`payment-transaction-fix` 작업으로 분리 완료) |

## 후속 정리 이력 (코드 분석 후 적용)

| 항목 | 파일 | 내용 |
|------|------|------|
| 데드 필드 제거 | `PaymentServiceV2` | `@Value("${payment.expire-minutes:10}") paymentExpireMinutes` — 주입했으나 실제 사용처 없음. `PaymentDomainEventListener`만 해당 값 사용 |
| `PaymentTxProcessor` 도입 | `application/service/PaymentTxProcessor.java` (신규) | `@Transactional(REQUIRES_NEW)` 메서드를 별도 Bean으로 분리. Spring AOP self-invocation 우회 문제를 해결하면서 선언적 트랜잭션 방식 유지 |
| 불필요 어노테이션 제거 | `PaymentQueryRepositoryImpl` | `@Repository` — Spring Data JPA가 네이밍 컨벤션(`*Impl`)으로 자동 감지하므로 불필요 |
| 무의미한 어노테이션 제거 | `Payment.updateStatus()` | `@Deprecated` — V1이 여전히 호출하므로 어노테이션이 의미 없음. 어노테이션 제거, V2 마이그레이션 완료 후 메서드 삭제 예정 |
| 레이어 위반 허용 (ADR) | `PaymentRedisKeys` | `infrastructure/redis/`에 위치하나 `application/`에서 참조. Port 과잉 설계 판단 → `docs/adr/001-payment-redis-key-layer.md`에 근거 기록 |
| 예외 범위 한정 | `PaymentServiceV2` | `catch (Exception e)` → 카카오 클라이언트: `RestClientException`, 주문 서비스: `FeignException`, DB 실패: `RuntimeException` |
| `@Order(1)` 추가 | `DistributedLockAspect` | `@Transactional`보다 먼저 실행되어 락이 트랜잭션 커밋 후 해제됨을 보장 |
| 트랜잭션 분리 | `refundPaymentByOrderId`, `compensateRefundByOrderId` | 카카오 취소 API를 `@Transactional` 밖으로 분리. DB 업데이트는 `requiresNewTransactionTemplate`으로 처리 |
| `PAYMENT_REFUND_FAIL` 추가 | `PaymentExceptionCode` | 환불 실패 전용 예외 코드 (HTTP 502) |
| 성공 경로 로그 제거 | `DistributedLockAspect` | 락 획득/해제 `log.info` 제거 |
| 알림 순서 수정 | `cancelPaymentByOrderId` | `payment.cancel()` → 주문 서비스 알림 순서로 변경 (DB 먼저, 알림은 best-effort) |

---

## 알려진 한계

| 항목 | 내용 |
|------|------|
| Redis AFTER_COMMIT 저장 실패 시 fallback 제한 | `onPaymentReady`에서 `RetryTemplate` 3회 재시도 후에도 실패하면 error log만 남음. Redis key 자체가 없으면 TTL 만료 이벤트가 발생하지 않아 `PaymentExpirationListener`도 동작 불가. stale PENDING 보정은 별도 보완 필요 (DB `expiresAt` 컬럼, 배치 등) |
| `TransactionTemplate` 전파 방식 | `TransactionConfig.java`에서 `REQUIRES_NEW`로 명시 등록 → 상위 트랜잭션 존재 여부와 무관하게 항상 독립 신규 트랜잭션 보장 (**해결됨**) |

---

## 결론

4개 Step 필수 항목 모두 구현에 반영. Match Rate **100%**, 기준 통과.

- Redis key와 만료 시간이 상수·설정으로 일원화됨
- 만료된 결제 승인 차단 (HTTP 410)
- DB 커밋 후에만 Redis key 설정 (트랜잭션 정합성 보장)
- 카카오 API 호출 구간에서 DB 커넥션 미점유, 보상 로직 추가
