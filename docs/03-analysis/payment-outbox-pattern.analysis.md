# Gap Analysis: payment-outbox-pattern

**분석일**: 2026-04-30
**설계 문서**: `docs/02-design/features/payment-outbox-pattern.design.md`
**Match Rate**: 100%

---

## 요약

Design 문서의 4개 Step 전 항목이 구현됨. Outbox 테이블 기반 트랜잭션-Kafka 분리, 릴레이 스케줄러, 만료 결제 배치까지 모두 동작 가능한 형태로 작성됨. 설계 대비 5건의 의도적 개선이 추가되었으나 모두 설계 의도(이벤트 무손실, 멱등 보정, 안전한 직렬화)를 강화하는 방향이며, 구조·계약·외부 동작 변경 없음.

```
┌──────────────────────────────────────────────────────┐
│  Overall Match Rate: 100%                            │
├──────────────────────────────────────────────────────┤
│  Step 1 (25%) Outbox 도메인 모델 + 릴레이 인프라:  25/25 ✓ │
│  Step 2 (25%) PaymentTxProcessor 전환:           25/25 ✓ │
│  Step 3 (25%) 릴레이 스케줄러 + 리스너 정리:      25/25 ✓ │
│  Step 4 (25%) Payment.expiresAt + 만료 배치:     25/25 ✓ │
└──────────────────────────────────────────────────────┘
```

**판정: PASS** (기준 90% 이상)

---

## 구현 완료 항목 (✅)

### Step 1: Outbox 도메인 모델 + 릴레이 인프라 (25/25)

| 항목 | 결과 | 비고 |
|------|:----:|------|
| `domain/model/OutboxStatus.java` — `PENDING/PUBLISHED/FAILED` enum | ✓ | 설계와 동일 |
| `domain/model/PaymentOutbox.java` — `@Entity`, `p_payment_outbox` 매핑 | ✓ | 필드 구성 일치 |
| `PaymentOutbox` `markPublished()`/`markFailed()`/`incrementRetry()` | ✓ | 시그니처 동일 |
| `BaseEntity` 미상속, `createdAt` 직접 선언 | ✓ | 설계 의도 반영 |
| `domain/repository/PaymentOutboxRepository.java` — `findTop50ByStatusOrderByCreatedAt` | ✓ | 시그니처 동일 |

### Step 2: PaymentTxProcessor 전환 (25/25)

| 항목 | 결과 | 비고 |
|------|:----:|------|
| `complete(UUID orderId)` `REQUIRES_NEW` + outbox 저장 | ✓ | `PaymentTxProcessor.java:36-41` |
| `fail(UUID orderId, String reason)` `REQUIRES_NEW` + outbox 저장 | ✓ | `PaymentTxProcessor.java:29-34` |
| `eventPublisher` (ApplicationEventPublisher) 의존성 제거 | ✓ | 클래스 필드 0건 |
| `PaymentOutboxRepository` 의존 추가 | ✓ | 생성자 주입 |
| `ObjectMapper` 의존 추가 | ✓ | 생성자 주입 |
| `refund()`/`cancel()` 변경 없음 (orderClient 직접 호출 경로) | ✓ | 설계 명시 그대로 유지 |

### Step 3: 릴레이 스케줄러 + 리스너 정리 (25/25)

| 항목 | 결과 | 비고 |
|------|:----:|------|
| `infrastructure/scheduler/OutboxRelayScheduler.java` 신규 | ✓ | `@Scheduled(fixedDelay = 3000)` |
| 단일 인스턴스 실행 보장 (분산 락) | ✓ | `RedissonClient.getLock("payment:outbox:relay:lock")` |
| `PENDING` 50건 조회 → 발행 → 상태 갱신 | ✓ | Loop 구조 동일 |
| `MAX_RETRY = 3` 초과 시 `markFailed()` | ✓ | `OutboxRecordProcessor.java:46-48` |
| `PaymentEventProducer.sendPaymentCompleted/sendPaymentFailed` 호출 | ✓ | `OutboxRecordProcessor#publish` |
| `@EnableScheduling` `PaymentApplication`에 추가 | ✓ | `PaymentApplication.java:7,12` |
| `PaymentDomainEventListener` Kafka 핸들러 2개(`onPaymentCompleted/onPaymentFailed`) 제거 | ✓ | 파일 내 0건 |
| `PaymentDomainEventListener.paymentEventProducer` 필드 제거 | ✓ | 의존성 0건 |
| `PaymentDomainEventListener.onPaymentReady` 유지 (Redis 키 설정) | ✓ | `PaymentDomainEventListener.java:29-40` |
| `domain/event/PaymentCompletedDomainEvent.java` 삭제 | ✓ | 디렉터리 내 미존재 |
| `domain/event/PaymentFailedDomainEvent.java` 삭제 | ✓ | 디렉터리 내 미존재 |
| `PaymentReadyDomainEvent` 유지 | ✓ | Redis 경로에 사용 중 |
| `PaymentExpirationListener` `paymentTxProcessor.fail()` 사용 | ✓ | `PaymentExpirationListener.java:39` |
| `PaymentEventProducer` 성공 경로 `log.info` 제거 | ✓ | 클래스 내 `log.info` 0건 |

### Step 4: Payment.expiresAt + 만료 배치 (25/25)

| 항목 | 결과 | 비고 |
|------|:----:|------|
| `Payment.expiresAt` 필드 추가 (nullable) | ✓ | `Payment.java:44` |
| `Payment.expireAt(LocalDateTime)` 메서드 추가 | ✓ | `Payment.java:50-52` |
| `PaymentServiceV2.paymentExpireMinutes` `@Value` 주입 | ✓ | `PaymentServiceV2.java:59-60` |
| `readyPayment()`에서 `payment.expireAt(now + N분)` 호출 | ✓ | `PaymentServiceV2.java:77` |
| `PaymentRepository.findByStatusAndExpiresAtBefore` | ✓ | `PaymentRepository.java:18` |
| `PaymentExpiredBatchScheduler` `@Scheduled(fixedDelay = 60_000)` | ✓ | `PaymentExpiredBatchScheduler.java:25` |
| 만료 결제마다 `paymentTxProcessor.fail()` (멱등) | ✓ | catch로 `IllegalStateException` 흡수 |

---

## Gap 목록

### 의도적 변경 (설계 → 구현 시 개선) — Match Rate 차감 없음

| # | 위치 | 설계 | 실제 구현 | 평가 |
|---|------|------|-----------|------|
| 1 | `OutboxRelayScheduler` 락 | `@DistributedLock(key = "'outbox:relay'")` | `RedissonClient.getLock("payment:outbox:relay:lock") + tryLock()` | 동일 목적. 직접 호출 방식이 명시적이고 단순함 |
| 2 | 릴레이 책임 분리 | `OutboxRelayScheduler` 단일 클래스 | `OutboxRelayScheduler`(락·조회) + `OutboxRecordProcessor`(직렬화·발행·상태 갱신·재시도) | SRP 강화. 테스트성 향상 |
| 3 | Outbox payload 생성 | `ofCompleted/ofFailed` 내 JSON 문자열 직접 조립 | `PaymentOutbox.of()` + `ObjectMapper.writeValueAsString(Map.of(...))` | 설계 "리스크" 항목 권장 사항 채택. 특수문자 안전 보장 |
| 4 | `PaymentDomainEventListener.retryTemplate` | "필드 제거" 명시 | 유지 | `onPaymentReady`의 Redis 키 설정에 여전히 사용. 설계 기술 오류 — 제거 시 회귀 발생 |
| 5 | `approvePayment` 보상 경로 | DB 업데이트 실패 시 카카오 보상 취소까지만 명시 | 보상 취소 후 `paymentTxProcessor.fail()` 추가 호출 | Outbox에 `PAYMENT_FAILED` 레코드 보장 → 후속 알림 경로 일관성 확보 |

### 미구현 / 불일치 항목

해당 없음. 설계 문서 명시 항목 중 누락·불일치 0건.

---

## 알려진 한계 (다음 이슈)

| # | 항목 | 내용 | 권장 후속 |
|---|------|------|-----------|
| L1 | 릴레이 지연 | 최대 3초 + Kafka 다운 시 `MAX_RETRY=3` 소진 후 `FAILED` 고착 | 운영 알림 + 재발행 운영 도구 |
| L2 | `FAILED` outbox 자동 보정 없음 | `status = FAILED` 레코드는 자동 재시도 대상 아님 | 수동 재발행 스크립트 또는 어드민 엔드포인트 |
| L3 | Consumer 멱등성 | 릴레이 재시도로 동일 이벤트 중복 가능 | 주문 서비스 수신 처리 멱등성 검증 |
| L4 | DB 마이그레이션 산출물 미확인 | `schema.sql` 코드 트리 내 미존재 | prod 적용 경로 문서화 또는 `ddl-auto` 적용 여부 명시 |
| L5 | `PaymentExpirationListener` fallback 한계 | Redis `set` 실패 시 TTL 이벤트 미발생 → 1분 배치만 보정 | "1분 배치 보정"으로 명확히 표현. 다중 인스턴스 시 배치 락 추가 필요 |

---

## 결론

설계 문서 4개 Step 전 항목 충족. **Match Rate 100%, PASS**.

핵심 효과:
- DB 커밋과 Kafka 발행 분리 → JVM 크래시에도 이벤트 영구 유실 없음
- `OutboxRelayScheduler` + `OutboxRecordProcessor` 분리로 책임 명확
- `PaymentExpiredBatchScheduler`로 Redis 키 누락 PENDING 결제 1분 내 보정
- `approvePayment` 보상 경로 강화로 외부 호출 실패 시에도 outbox 기록 보장
