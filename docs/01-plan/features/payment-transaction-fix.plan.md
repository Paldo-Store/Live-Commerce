# Plan: 결제 트랜잭션 추가 개선

**작성일**: 2026-04-20
**대상 서비스**: `payment/`
**우선순위**: High

---

## 배경

`payment-transaction-integrity` 사이클에서 `approvePayment`의 트랜잭션 문제를 해결했으나,
코드 분석 결과 동일하거나 유사한 문제가 다른 메서드에도 존재함이 확인됨.

---

## 문제 목록

| ID | 심각도 | 위치 | 문제 |
|----|--------|------|------|
| T1 | High | `PaymentServiceV2.refundPaymentByOrderId` | 카카오 취소 API가 `@Transactional` 내 존재 |
| T2 | High | `PaymentServiceV2.compensateRefundByOrderId` | 카카오 취소 API가 `@Transactional` 내 존재 |
| T3 | Medium | `PaymentExpirationListener.onMessage` | `updateStatus(FAILED)` 직접 호출로 도메인 상태 전이 검증 우회 |
| T4 | Medium | `DistributedLockAspect` | `@Order` 미설정으로 `@Transactional`과 실행 순서 미보장 |
| T5 | Low | `DistributedLockAspect`, `PaymentEventConsumer`, `PaymentExpirationListener` | 성공 경로 `log.info` |

---

## 목표

- 카카오 외부 API 호출 구간에서 DB 커넥션 미점유
- 카카오 취소 성공 → DB 실패 시 정합성 보장
- 도메인 상태 전이 규칙이 모든 경로에서 일관되게 적용
- 분산 락이 트랜잭션 커밋 이후 해제됨을 보장

---

## 범위

### 포함

- `PaymentServiceV2`: `refundPaymentByOrderId`, `compensateRefundByOrderId` 트랜잭션 분리
- `PaymentExpirationListener`: `fail()` 상태 전이 메서드 사용
- `DistributedLockAspect`: `@Order` 추가
- 성공 경로 `log.info` 제거 3곳

### 제외

- V1 `PaymentService` (별도 이슈)
- Outbox 패턴 도입 (보상 취소 실패 완전 해결 — 별도 이슈)

---

## 완료 조건

- [x] T1: `refundPaymentByOrderId` — 카카오 API/DB 트랜잭션 분리
- [x] T2: `compensateRefundByOrderId` — 카카오 API/DB 트랜잭션 분리
- [x] T3: `PaymentExpirationListener` — `payment.fail()` 사용
- [x] T4: `DistributedLockAspect` — `@Order(1)` 추가
- [x] T5: 성공 경로 `log.info` 제거
- [x] 컴파일 통과

**완료일**: 2026-04-23

---

## 리스크

| 리스크 | 대응 |
|--------|------|
| T1/T2 분리 후 카카오 취소 성공 → DB 실패 | 에러 로그 기록. Outbox 패턴은 별도 이슈 |
| T4 `@Order` 변경으로 기존 동작 영향 | `readyPayment`만 `@DistributedLock` + `@Transactional` 병용 — 영향 범위 제한적 |
