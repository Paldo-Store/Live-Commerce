# Plan: 결제 도메인 리팩토링

**작성일**: 2026-04-17  
**대상 서비스**: `client/payment`  
**우선순위**: 높음 — 버그 + dead code + 레이어 위반이 혼재

---

## 1. 배경 및 목적

코드 분석 결과, 결제 서비스에 다음 문제들이 누적됨:
- V1/V2 서비스가 병존하면서 어느 것이 현재 활성인지 불명확
- 테스트용 코드가 프로덕션 코드에 그대로 남아있음
- 상태 전이에 유효성 검사가 없어 데이터 정합성 위험
- Application layer가 Redis infrastructure를 직접 의존
- 실제 버그 2개 발견

---

## 2. 발견된 문제 목록

### 🔴 버그 (즉시 수정 필요)

| # | 위치 | 문제 |
|---|------|------|
| B1 | `PaymentService.approvePayment():82` | PENDING이 아닌 모든 상태(COMPLETED 포함)를 FAILED로 덮어씀. 이미 완료된 결제가 실패로 변경될 수 있음 |
| B2 | `PaymentService.validatePaymentSearchPermission():239` | `isSelf(userDetails.getUserId(), userDetails)` — 인자가 동일 객체라 항상 true. 권한 검사 무력화 |

### 🟠 Dead Code (제거 대상)

| # | 파일 | 이유 |
|---|------|------|
| D1 | `LockTestService.java` | JMeter 부하 테스트용. `System.err.println`, `Thread.sleep` 포함. 프로덕션에 있으면 안 됨 |
| D2 | `LockTestController.java` | LockTestService 사용 컨트롤러. 동일하게 제거 |
| D3 | `PaymentServiceV2NoLock.java` | 락 없는 비교 실험용. 실제 로직에서 사용되지 않음 |
| D4 | `PaymentService.java` (V1) | V2로 교체됨. OrderClient Feign 기반 동기 호출 방식 |
| D5 | `PaymentController.java` (V1) | `/api/v1/payments` — V1 서비스를 참조. V2로 통합 필요 |
| D6 | `PaymentReadyController.java` | 역할 불명확. 기존 Controller와 중복 여부 확인 필요 |

### 🟡 레이어 위반 (아키텍처 개선)

| # | 위치 | 문제 |
|---|------|------|
| L1 | `PaymentService:50`, `PaymentServiceV2:51` | Application service가 `RedissonClient` (infrastructure) 직접 의존. Redis TTL 설정 로직을 infrastructure 레이어로 이동 필요 |
| L2 | `PaymentReadyRequestDto.toEntity()` | DTO에 도메인 객체 생성 로직 포함. `Payment.of()` 정적 팩토리 또는 서비스에서 처리해야 함 |
| L3 | `PaymentRefundResponseDto.java` | `application/dto/request` 패키지에 위치. `response` 패키지로 이동 필요 |

### 🟡 중복 코드 (코드 품질)

| # | 위치 | 문제 |
|---|------|------|
| C1 | `validatePaymentGetPermission`, `validatePaymentRefundPermission`, `validatePaymentCancelPermission` | 동일한 로직 3개 중복. 단일 메서드로 통합 가능 |
| C2 | `"payment:expire:" + dto.orderId()` | 두 서비스 파일에 Redis 키 패턴 하드코딩. 상수로 추출 필요 |

### 🟡 도메인 모델 취약점

| # | 위치 | 문제 |
|---|------|------|
| M1 | `Payment.updateStatus()` | 상태 전이 유효성 검사 없음. 임의의 상태 변경 허용. 유효한 전이 정의 필요 (`PENDING→COMPLETED`, `COMPLETED→REFUND` 등) |

### 🟡 V2 서비스 불일치

| # | 위치 | 문제 |
|---|------|------|
| V1 | `PaymentServiceV2.cancelPaymentByOrderId():189` | V2인데 취소 알림을 OrderClient Feign으로 직접 호출. Kafka 이벤트로 통일 필요 |
| V2 | `PaymentServiceV2.refundPaymentByOrderId():171` | 동일하게 OrderClient 직접 호출 잔존 |

---

## 3. 개선 범위 및 우선순위

### Phase 1 — 버그 수정 + Dead Code 제거 ✅ 완료
- B1 수정: `approvePayment()` FAILED 덮어쓰기 버그
- B2 수정: `validatePaymentSearchPermission()` isSelf 무력화 → 메서드 제거
- D1~D5 dead code 제거: LockTestService, LockTestController, PaymentServiceV2NoLock, PaymentReadyController, MockKakaoPayClient

### Phase 2 — 코드 정리 (이슈 범위)
- L3: `PaymentRefundResponseDto` 패키지 이동 (`request` → `response`)
- C1: 권한 검사 메서드 3개 중복 통합
- C2: Redis 키 패턴 상수 추출

### 별도 이슈로 분리
- V1 제거: order 서비스 Feign 클라이언트 의존 중 — 별도 협의 필요
- cancel/refund Kafka 전환: 동작 변경으로 QA 필요
- 상태 전이 유효성: 도메인 모델 변경으로 영향도 검토 필요

---

## 4. 리스크

| 리스크 | 수준 | 대응 |
|--------|------|------|
| V1 제거 시 다른 서비스가 `/api/v1/payments` 호출 중일 수 있음 | 중 | 제거 전 gateway 라우팅 및 타 서비스 코드 확인 필요 |
| `PaymentReadyController` 제거 시 실제 사용 중일 수 있음 | 중 | git log + 코드 참조 확인 후 진행 |
| 상태 전이 추가 시 기존 보상 로직과 충돌 가능 | 중 | `compensateRefundByOrderId` 흐름 먼저 검토 |

---

## 5. 작업 제외 범위

- Kafka 이벤트 스키마 변경 (타 서비스 영향도 큼)
- 카카오페이 외 PG사 추가 (신규 기능)
- DB 스키마 변경

---

## 6. 다음 단계

`/pdca design payment-domain-refactor` 로 Phase별 구현 설계 진행
