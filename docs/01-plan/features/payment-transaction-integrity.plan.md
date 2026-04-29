# Plan: 결제 트랜잭션 정합성 개선

**작성일**: 2026-04-20  
**대상 서비스**: `payment/`  
**우선순위**: 높음 — 실제 운영 시 결제 데이터 정합성 문제로 직결

---

## 1. 배경 및 목적

코드 리뷰를 통해 결제 서비스의 트랜잭션 경계 설계에 4가지 문제가 발견됨.  
기능은 동작하나 특정 시나리오에서 **정합성 오류, 결제 만료 우회, 커넥션 낭비**가 발생할 수 있다.

---

## 2. 발견된 문제

### T1 — 결제 만료 미검증

| 항목 | 내용 |
|------|------|
| 위치 | `PaymentServiceV2.approvePayment()` |
| 문제 | `readyPayment`에서 Redis에 10분 TTL key를 설정하지만, `approvePayment`에서 해당 key 존재 여부를 확인하지 않음. 10분이 지난 결제도 승인 가능 |
| 위험도 | 중 — 만료된 결제 승인 허용 |

### T2 — 외부 API + DB 동일 트랜잭션

| 항목 | 내용 |
|------|------|
| 위치 | `PaymentServiceV2.approvePayment()` (`@Transactional`) |
| 문제 | 카카오 API 호출(`requestKakaoPayApprove`)이 `@Transactional` 내부에 있음. DB 커넥션을 HTTP 응답 대기 시간 동안 점유. 카카오 성공 후 DB commit 실패 시 결제는 처리됐으나 DB 미반영 |
| 위험도 | 높음 — 커넥션 풀 고갈 + 결제-DB 정합성 오류 가능 |

### T3 — DB + Redis 트랜잭션 불일치

| 항목 | 내용 |
|------|------|
| 위치 | `PaymentServiceV2.readyPayment()` (`@Transactional`) |
| 문제 | `paymentRepository.save()`(DB)와 `bucket.set()`(Redis)가 같은 트랜잭션 안에 있음. Redis는 Spring 트랜잭션에 참여하지 않으므로 DB 롤백 시 Redis key가 잔존하거나, Redis 실패 시 DB만 커밋될 수 있음 |
| 위험도 | 중 — 유령 Redis key 또는 누락된 만료 처리 |

### T4 — 결제 유효시간 하드코딩

| 항목 | 내용 |
|------|------|
| 위치 | `PaymentServiceV2` L73, `PaymentExpirationListener` 외 |
| 문제 | `10, TimeUnit.MINUTES`가 코드에 직접 박혀 있음. 정책 변경 시 코드 수정 + 재배포 필요 |
| 위험도 | 낮음 — 유지보수 비용 |

---

## 3. 해결 방향

### Step 1: T4 상수화 (가장 단순, 먼저 처리)
- `PAYMENT_EXPIRE_MINUTES = 10` 상수를 `PaymentServiceV2`와 `PaymentExpirationListener`에서 공유 가능한 위치에 추출
- application.yml `payment.expire-minutes: 10`으로 외부화하고 `@Value`로 주입 (정책 변경 시 재배포 불필요)

### Step 2: T1 Redis 만료 검증
- `approvePayment` 진입 시 `payment:expire:{orderId}` key 존재 여부 확인
- key 없음 → 결제 창 만료 → 새 exception code `PAYMENT_EXPIRED` throw
- 승인 성공 후 Redis key 명시적 삭제 (TTL 자연 만료 전 제거)

### Step 3: T3 Redis를 AFTER_COMMIT으로 분리
- `readyPayment`에서 `bucket.set()` 직접 호출 제거
- DB 커밋 완료 후 `PaymentReadyDomainEvent`를 publish
- `PaymentDomainEventListener`에 `@TransactionalEventListener(AFTER_COMMIT)` 핸들러 추가 → Redis key 설정
- 이미 구축된 Domain Event 인프라 재사용

### Step 4: T2 외부 API와 DB 트랜잭션 분리
- `approvePayment`에서 `@Transactional` 제거
- `TransactionTemplate`을 주입해 DB write 구간만 프로그래밍 방식으로 트랜잭션 적용
- 흐름:
  ```
  1. [no tx]  Payment 조회 + 상태 검증 + Redis key 검증
  2. [no tx]  카카오 API 호출 (외부 HTTP)
  3. [new tx] DB 상태 변경 + 도메인 이벤트 publish (TransactionTemplate)
  4. [no tx]  Redis key 삭제
  ```
- Kafka 이벤트는 3번 트랜잭션 커밋 후 `@TransactionalEventListener(AFTER_COMMIT)`에서 발행 (기존 구조 유지)

---

## 4. 리스크

| 리스크 | 수준 | 대응 |
|--------|------|------|
| T2: 카카오 성공 → DB 실패 시 보상 로직 필요 | 높음 | Step 4 구현 시 DB 실패 catch → 카카오 취소 API 호출로 보상 |
| T3: AFTER_COMMIT 이후 Redis 실패 시 만료 미설정 | 중 | 실패 로그 경보 + `PaymentExpirationListener` 가 PENDING을 TTL 대신 처리하는 기존 fallback 유지 |
| Step 4 TransactionTemplate: 기존 `@DistributedLock`과 결합 검토 필요 | 중 | `readyPayment`만 `@DistributedLock`, `approvePayment`는 락 없음 → 영향 없음 |
| application.yml 변경(Step 1) → 기존 동작 확인 | 낮음 | 기본값 10분 유지 |

---

## 5. 완료 조건

- [ ] Step 1: `payment.expire-minutes` 설정으로 유효시간 제어 가능
- [ ] Step 2: 만료된 결제(Redis key 없음) 승인 시도 시 `PAYMENT_EXPIRED` 예외 반환
- [ ] Step 3: `readyPayment` 내 Redis 직접 호출 제거, DB 롤백 시 Redis key 미설정
- [ ] Step 4: `approvePayment`에서 카카오 API 호출 중 DB 커넥션 미점유
- [ ] 컴파일 통과, drift-check 레이어 위반 0건

---

## 6. 제외 범위

- Outbox Pattern (신뢰성 보장 2단계) — 별도 이슈
- 카카오 외 PG사 추가
- V1 서비스 수정 (불변 원칙 유지)

---

## 7. 다음 단계

`/pdca design payment-transaction-integrity` 로 파일별 변경 세부 설계 진행
