# ADR-001: PaymentRedisKeys 위치 결정

**날짜**: 2026-04-22  
**상태**: 수용됨

---

## 맥락

`PaymentRedisKeys`는 Redis key prefix 상수(`"payment:expire:"`)를 담는 클래스다.  
이 상수를 `PaymentServiceV2`(application), `PaymentDomainEventListener`(infrastructure), `PaymentExpirationListener`(infrastructure) 등 여러 곳에서 참조한다.

레이어 의존 방향 원칙상 `application → infrastructure` 참조는 금지다.  
상수를 `infrastructure/redis/`에 두면 `application/service/`가 이를 참조해 위반이 발생한다.

---

## 검토한 선택지

### 1. Port 인터페이스 (`application/port/PaymentCachePort`)
```
application/port/PaymentCachePort     ← 인터페이스
infrastructure/redis/PaymentCacheAdapter ← 구현체
```
- 장점: 레이어 의존 방향 완전 준수, 테스트 시 mock 교체 가능
- 단점: Redis는 교체 가능성이 거의 없는 인프라. 실무에서 Redis에 Port를 두는 경우는 드물다. `isPaymentExpireKey()`, `extractOrderIdFromKey()` 같은 메서드가 Redis key 구조를 인터페이스에 노출해 추상화가 의미를 잃는다.

### 2. `domain/PaymentRedisKeys`
- 장점: 모든 레이어가 참조 가능
- 단점: Redis key는 인프라 개념이다. domain은 순수 비즈니스 로직이어야 하므로 의미상 맞지 않는다.

### 3. `application/constant/PaymentRedisKeys`
- 장점: application에서 참조 가능
- 단점: 비표준 패키지. Redis 상수가 application 레이어에 존재하는 것도 어색하다.

### 4. `infrastructure/redis/PaymentRedisKeys` (채택)
- 장점: 상수가 의미상 맞는 위치에 존재. 코드 변경량 최소.
- 단점: `application/service/`에서 `infrastructure/redis/`를 참조하는 레이어 위반 발생.

---

## 결정

**`infrastructure/redis/PaymentRedisKeys`에 두고 레이어 위반을 실용적 타협으로 수용한다.**

### 근거

- Redis는 이 서비스에서 교체될 가능성이 없다. Port 추상화가 가져오는 이점(구현체 교체, mock 테스트)이 현실화될 가능성이 낮다.
- `RedissonClient`도 이미 `application/service/`에서 직접 주입받아 사용 중이다. 상수 하나만 분리해도 근본적 구조는 바뀌지 않는다.
- 실무에서 Redis, JPA 등 교체 가능성이 낮은 인프라는 Port 없이 직접 사용하는 게 일반적이다. Port가 의미있는 대상은 카카오페이처럼 다른 구현체로 교체될 수 있는 외부 시스템이다.

### 향후 재검토 조건

- Redis를 다른 캐시로 교체해야 하는 상황이 생기면 Port 패턴 적용 검토
- 단위 테스트에서 Redis mock이 필요해지면 Port 패턴 적용 검토

---

## 결과

`application/service/`가 `infrastructure/redis/PaymentRedisKeys`를 참조하는 구조가 유지된다.  
코드 리뷰에서 지적받을 수 있는 부분이나, 위 근거로 설명 가능하다.
