# payment-web

결제 파트만 독립적으로 테스트하는 React 앱이다.

## 경로

- 시작 화면: `/`
- 결제 승인 콜백: `/payments/approve`
- 결제 취소 콜백: `/payments/cancel`
- 결제 실패 콜백: `/payments/fail`

## 로컬 기준

1. `payment` 서비스의 `frontend.base-url`이 `http://localhost:5173`을 가리키게 둔다.
2. 이 앱은 Vite dev server `5173` 포트에서 띄운다.
3. `/api` 요청은 gateway `http://localhost:19091`으로 프록시된다.

## 필요한 입력값

- `Bearer` access token
- 주문 생성용 `broadcastId`, `productId` 또는 조회 결과에서 선택

gateway가 JWT를 검증한 뒤 `X-User-Id`, `X-User-Username`, `X-User-Role`를 내부 헤더로 자동 주입한다.
프론트는 `주문 생성 -> orderId 확보 -> ready -> 카카오 redirect -> approve` 흐름으로 동작한다.

## 주의

- 주문 서비스는 `LIVE` 상태의 방송에서만 주문 생성이 된다.
- 저장소의 더미 SQL에는 `SCHEDULED` 방송이 많아서, 목록 조회 결과가 있어도 주문 생성은 방송 상태에 따라 실패할 수 있다.
- 이 경우 실제 `LIVE` 방송 ID를 넣거나, 방송 상태를 `LIVE`로 만든 뒤 테스트해야 한다.
