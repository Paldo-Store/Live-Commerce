# Plan: payment Kafka 이벤트 유실 방지를 위한 Outbox Pattern 도입

**작성일**: 2026-04-23  
**대상 서비스**: `payment/`  
**우선순위**: 높음 — 앱 크래시 시 Kafka 이벤트 유실로 결제-주문 정합성 오류 직결  
**참조 이슈**: `payment-transaction-integrity` Step 3·4 "별도 이슈" 항목

---

## 1. 배경 및 목적

`payment-transaction-integrity` 작업으로 DB 커밋 후 Kafka 이벤트를 발행하는 구조(`@TransactionalEventListener(AFTER_COMMIT)`)가 도입됐다. 이 구조는 DB 롤백 시 이벤트가 발행되지 않는다는 점에서 기존보다 안전하지만, **DB 커밋과 Kafka publish 사이 앱이 비정상 종료되면 이벤트가 영구 유실**된다.

현재 누락된 보완 장치:
- Kafka 이벤트 발행 실패 시 재시도 메커니즘 없음
- Redis AFTER_COMMIT 실패(PaymentReadyDomainEvent) 시 stale PENDING 보정 수단 없음

---

## 2. 발견된 문제

### O1 — Kafka 이벤트 유실

| 항목 | 내용 |
|------|------|
| 위치 | `PaymentDomainEventListener` (`@TransactionalEventListener(AFTER_COMMIT)`) |
| 문제 | DB 커밋 성공 후 JVM 크래시 또는 Kafka 브로커 일시 불가 시 이벤트 발행 기회가 사라짐. Spring의 `@TransactionalEventListener`는 이벤트 발행 실패에 대한 재시도를 보장하지 않음 |
| 영향 | 결제 완료/실패/취소 이벤트가 주문 서비스에 전달되지 않아 주문 상태가 PENDING으로 고착 |
| 위험도 | 높음 |

### O2 — Redis 만료 key 미설정 시 stale PENDING

| 항목 | 내용 |
|------|------|
| 위치 | `PaymentDomainEventListener.onPaymentReady()`, `PaymentExpirationListener` |
| 문제 | Redis AFTER_COMMIT 저장 3회 재시도 실패 시 `payment:expire:{orderId}` key가 설정되지 않음. key가 없으면 TTL 만료 이벤트가 발생하지 않아 `PaymentExpirationListener`도 동작 불가. 결제가 PENDING으로 영구 잔존 |
| 위험도 | 중 — 운영 중 발생 빈도는 낮으나 수동 보정 필요 |

---

## 3. 해결 방향

### Step 1: Outbox 테이블 도입 (O1 해결)

DB 트랜잭션과 같은 커밋 안에 이벤트를 outbox 테이블에 저장, 별도 릴레이가 Kafka로 발행.

```
[결제 상태 변경 TX]
  └─ payment 상태 UPDATE
  └─ payment_outbox INSERT (eventType, payload, status=PENDING)

[OutboxRelay — 별도 스케줄러 또는 CDC]
  └─ outbox PENDING 조회
  └─ Kafka publish
  └─ outbox status=PUBLISHED 업데이트
```

- `payment_outbox` 스키마: `id`, `aggregate_id`, `event_type`, `payload`(JSON), `status`(`PENDING`/`PUBLISHED`/`FAILED`), `created_at`, `published_at`
- 릴레이 방식: **스케줄러 폴링** (Debezium CDC는 인프라 추가 필요 → 제외 범위)
- 릴레이는 `PENDING` 레코드를 배치 조회 후 Kafka 발행 → 성공 시 `PUBLISHED`, 재시도 한계 초과 시 `FAILED` + error log

### Step 2: Payment `expiresAt` 컬럼 추가 (O2 해결)

Redis key 미설정 케이스를 DB 기반으로 보정.

- `payment` 테이블에 `expires_at` 컬럼 추가 (`readyPayment` 시 현재 시각 + 만료 시간으로 설정)
- 배치 또는 스케줄러: `status = PENDING AND expires_at < NOW()` 조회 → 만료 처리(`FAILED`)
- `PaymentExpirationListener`의 Redis TTL 경로와 병행 운영 (중복 처리 방지를 위한 낙관적 락 또는 상태 조건 check)

---

## 4. 리스크

| 리스크 | 수준 | 대응 |
|--------|------|------|
| Outbox 릴레이와 기존 `@TransactionalEventListener` 이중 발행 | 높음 | Step 1 완료 후 `PaymentDomainEventListener` Kafka 발행 코드 제거. 단계적 전환 (outbox → 검증 → 기존 제거) |
| outbox 폴링 지연으로 주문 서비스 상태 갱신 지연 | 중 | 폴링 주기 1~5초로 설정. SLA 요건 확인 필요 |
| `expiresAt` 배치와 `PaymentExpirationListener` 중복 FAILED 처리 | 중 | `payment.fail()` 내 상태 조건 체크(`status == PENDING`이면 처리) → 멱등성 보장 |
| DB 스키마 변경 (`payment_outbox` 신규, `payment.expires_at` 추가) | 중 | 마이그레이션 스크립트 별도 작성. V1/V2 호환성 확인 |

---

## 5. 완료 조건

- [ ] Step 1: 앱 크래시 시뮬레이션 후 재기동 시 Kafka 이벤트 재발행 확인
- [ ] Step 1: `PaymentDomainEventListener`의 직접 Kafka 발행 코드 제거 완료
- [ ] Step 1: outbox `FAILED` 레코드 발생 시 error log 출력
- [ ] Step 2: Redis key 미설정 PENDING 결제가 `expiresAt` 기준 배치로 정상 만료 처리됨
- [ ] 컴파일 통과, drift-check 레이어 위반 0건

---

## 6. 제외 범위

- Debezium / CDC 기반 릴레이 (인프라 추가 필요)
- Kafka 메시지 중복 처리 (Consumer 멱등성은 수신 서비스 책임)
- V1 서비스 수정

---

## 7. 다음 단계

`/pdca design payment-outbox-pattern` 으로 파일별 변경 세부 설계 진행
