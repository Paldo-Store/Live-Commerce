# Plan: 토스페이먼츠 결제 수단 추가 및 PG사 추상화

**작성일**: 2026-05-11  
**대상 서비스**: `payment/`  
**우선순위**: 중 — 결제 수단 다양화로 전환율 개선  
**참조 이슈**: PDSH-17

---

## 1. 배경 및 목적

현재 결제 서비스는 카카오페이만 지원한다. `PaymentServiceV2`가 `KakaoPayClient`를 직접 주입받는 구조로, PG사를 추가하려면 서비스 로직 전체에 분기(`if 카카오 / else if 토스`)가 침투하게 된다.

토스페이먼츠를 추가하면서 동시에 PG사 추상화 레이어를 도입, 이후 PG사 추가 시 서비스 로직 변경 없이 구현체만 추가할 수 있는 구조로 개선한다.

---

## 2. 문제 정의

### P1 — PG사 강결합

| 항목 | 내용 |
|------|------|
| 위치 | `PaymentServiceV2`, `application/port/KakaoPayClient.java` |
| 문제 | 포트 인터페이스 자체가 카카오페이 전용 메서드명·DTO를 노출. 서비스가 `KakaoPayClient`를 직접 의존하여 PG사 교체·추가가 불가 |
| 영향 | 토스 추가 시 서비스 레이어에 PG사 분기 로직 침투 불가피 |

### P2 — 결제 수단 미저장

| 항목 | 내용 |
|------|------|
| 위치 | `Payment` 도메인 모델 |
| 문제 | `Payment` 엔티티에 결제 수단(`paymentMethod`) 컬럼 없음. 어떤 PG사로 결제했는지 추적 불가 |
| 영향 | 환불 시 어느 PG사 API를 호출해야 하는지 알 수 없음 |

---

## 3. 해결 방향

### Step 1: PG사 추상화 포트 도입

`KakaoPayClient` 포트를 PG사 중립적인 `PaymentGateway`로 교체.

```
application/port/PaymentGateway.java         ← 신규: 공통 인터페이스
application/port/PaymentGatewayFactory.java  ← 신규: paymentMethod → 구현체 라우팅 (application 레이어)
infrastructure/client/KakaoPayGateway.java   ← 기존 KakaoPayClientImpl → 이름 변경·구현
infrastructure/client/TossPayGateway.java    ← 신규: 토스페이먼츠 구현체
```

`PaymentGatewayFactory`를 `infrastructure/`에 두면 `PaymentServiceV2`(application)가 infrastructure를 직접 참조하게 되어 레이어 의존 방향 위반. `application/port/`에 위치.

`PaymentGateway` 공통 메서드:
- `ready(userId, orderId, amount, itemName)` → `PaymentReadyResult`
- `approve(tid, pgToken, orderId, userId)` → `PaymentApproveResult`
- `cancel(tid, amount)` → `PaymentCancelResult`
- `supports(PaymentMethod)` → 해당 PG사 지원 여부

### Step 2: Payment 도메인 모델에 paymentMethod 추가

`Payment` 엔티티에 `paymentMethod` 컬럼(`KAKAO`, `TOSS`) 추가.  
환불/취소 시 `payment.getPaymentMethod()`로 PG사 선택.

### Step 3: 토스페이먼츠 API 연동

토스페이먼츠 결제창 API(v1) 기준 구현:
- 결제 준비: `POST /v1/payments` (클라이언트 사이드 SDK 방식이므로 서버는 승인만 처리)
- 결제 승인: `POST /v1/payments/confirm`
- 결제 취소: `POST /v1/payments/{paymentKey}/cancel`

인증: `Authorization: Basic {Base64(secretKey:)}`

### Step 4: PaymentServiceV2 의존성 교체

`KakaoPayClient` → `PaymentGatewayFactory` 주입으로 교체.  
서비스 내 PG사 분기 없음 — 팩토리가 `paymentMethod`로 구현체 선택.

---

## 4. 리스크

| 리스크 | 수준 | 대응 |
|--------|------|------|
| 기존 카카오페이 결제 흐름 회귀 | 높음 | KakaoPayGateway로 리네이밍 후 기존 테스트 유지 |
| DB 스키마 변경 (`payment_method` 컬럼 추가) | 중 | `NOT NULL DEFAULT 'KAKAO'` — 기존 데이터 호환 |
| 토스 샌드박스 키 미확보 | 중 | `mock` 프로파일로 Mock 구현체 우선 개발 |
| 기존 카카오 DTO가 공통 Result로 교체 시 매핑 누락 | 중 | 컴파일 타임에 탐지 가능 |

---

## 5. 완료 조건

- [x] `PaymentGateway` 포트로 카카오·토스 모두 동작
- [x] `Payment` 엔티티에 `paymentMethod` 저장 및 환불 시 올바른 PG사 호출
- [x] `KakaoPayClient` 포트 및 카카오 전용 DTO 제거
- [x] 컴파일 통과
- [x] 레이어 위반 0건 (`PaymentGatewayFactory` → `application/port/`)

---

## 6. 제외 범위

- 토스 위젯 연동 (클라이언트 사이드 SDK)
- 부분 취소
- V1 서비스(`PaymentService`) 수정
- 실 결제 검증 (샌드박스 환경에서만 테스트)

---

## 7. 다음 단계

`/pdca design toss-payment` 으로 파일별 변경 세부 설계 진행
