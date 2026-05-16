# CLAUDE.md

**이 리포지터리에서 작업하는 AI 에이전트를 위한 진입점입니다.**

이 파일은 Live Commerce Platform 코드베이스를 탐색하는 **지도** 역할을 합니다. 백과사전이 아닌 목차로 생각하세요.

---

## 🎯 핵심 철학

**에이전트 우선 개발**: 이 코드베이스는 에이전트의 가독성과 자율적 작동에 최적화되어 있습니다.

- **모든 것이 리포지터리 안에 있음**: 이 리포지터리에 버전 관리되지 않으면, 에이전트에게는 존재하지 않습니다
- **문서가 진실의 원천**: 코드는 문서화된 내용을 반영해야 합니다
- **아키텍처는 기계적으로 강제됨**: 테스트, 린터, CI를 통해 강제합니다
- **빠른 피드백 루프**: 에이전트가 로컬에서 빌드, 테스트, 검증할 수 있습니다

---

## 📍 네비게이션 맵

### 여기서 시작하세요
- **[ARCHITECTURE.md](ARCHITECTURE.md)** - 시스템 아키텍처, 서비스 경계, 레이어링 규칙
- **[CLAUDE.md](CLAUDE.md)** - Claude Code 전용 가이드 및 명령어

### 핵심 문서
모든 문서는 `docs/`에 있으며 에이전트가 찾기 쉽게 구조화되어 있습니다:

```
docs/
├── design-docs/        # 아키텍처 결정 및 패턴
├── product-specs/      # 제품 요구사항 및 사용자 플로우
├── exec-plans/         # 현재 및 완료된 실행 계획
├── references/         # 외부 API 문서 및 라이브러리 가이드
└── quality/           # 품질 메트릭 및 개선 추적
```

### 주요 파일
- **[docs/DESIGN.md](docs/DESIGN.md)** - 설계 원칙 및 엔지니어링 표준
- **[docs/MSA_PATTERNS.md](docs/MSA_PATTERNS.md)** - 마이크로서비스 통신 패턴
- **[docs/KAFKA_EVENTS.md](docs/KAFKA_EVENTS.md)** - 이벤트 기반 아키텍처 가이드
- **[docs/quality/QUALITY_SCORE.md](docs/quality/QUALITY_SCORE.md)** - 서비스별 품질 추적

---

## 🏗️ 아키텍처 개요

**MSA (마이크로서비스 아키텍처)** - 13개의 독립적인 서비스:

### 서비스 카테고리
- **인프라**: gateway, eureka, config
- **비즈니스**: user, product, order, payment, company, coupon
- **실시간**: livebroadcast, chat, ai
- **교차 관심사**: notification

### 통신 방식
- **동기**: Feign Client (REST)
- **비동기**: Kafka (이벤트)
- **디스커버리**: Eureka
- **게이트웨이**: 모든 요청 → 포트 19091

**전체 세부사항**: [ARCHITECTURE.md](ARCHITECTURE.md)

---

## 📦 서비스 구조 (레이어드 아키텍처)

각 서비스는 이 **필수** 구조를 따릅니다:

```
src/main/java/com/live_commerce/<service>/
├── presentation/       # 컨트롤러, REST 엔드포인트
├── application/        # DTO, 서비스, 비즈니스 로직
├── domain/            # 엔티티, 리포지터리 (JPA)
└── infrastructure/    # 설정, 보안, 외부 클라이언트
```

**규칙**:
- 의존성은 **하향으로만** 흐름: presentation → application → domain
- 순환 의존성 금지
- 교차 관심사는 infrastructure 레이어를 통해 처리

**강제 방법**: 각 서비스의 구조 테스트가 레이어링을 검증합니다

---

## 🔧 일반적인 개발 작업

### 빌드 & 테스트
```bash
./gradlew build                    # 모든 서비스 빌드
./gradlew :order:build            # 특정 서비스 빌드
./gradlew test                    # 모든 테스트 실행
./gradlew :product:test           # 특정 서비스 테스트
```

### 로컬 실행
```bash
docker-compose up -d              # 인프라 시작 (DB, Kafka, Redis)
cd <service> && ./gradlew bootRun # 서비스 로컬 실행
```

### QueryDSL (사용하는 서비스의 경우)
```bash
./gradlew clean                   # 생성된 Q-클래스 정리
./gradlew build                   # Q-클래스 재생성
```

---

## 🎨 코드 스타일 & 표준

### 도구를 통한 강제
- **Lombok**: 보일러플레이트 감소 (@Getter, @Builder 등)
- **QueryDSL**: 타입 안전 쿼리 (order, product 서비스)
- **Swagger**: API 문서화 (자동 생성)
- **JUnit 5**: 테스팅 프레임워크

### 컨벤션
- **DTO**: `application/dto/`에 Request/Response 분리
- **네이밍**: `<Entity><Action><Type>` (예: `OrderCreateRequest`)
- **예외**: 서비스별 커스텀 예외 코드
- **로깅**: 구조화된 로깅 (SLF4J)

---

## 🔄 이벤트 기반 패턴 (Kafka)

**주요 이벤트 플로우**:

1. **Order → Product**: 주문 생성 → 재고 감소
2. **Order → Coupon**: 주문 생성 → 쿠폰 사용
3. **Payment → Order**: 결제 성공/실패 → 주문 상태 업데이트
4. **User → Coupon**: 회원가입 → 환영 쿠폰 발급
5. **Broadcast → Notification**: 방송 시작 → 구독자 알림

**전체 이벤트 카탈로그**: [docs/KAFKA_EVENTS.md](docs/KAFKA_EVENTS.md)

---

## 🧪 테스팅 철학

### 테스트 피라미드
- **단위 테스트**: `application/service/`의 비즈니스 로직
- **통합 테스트**: 테스트 DB와 함께 `@SpringBootTest`
- **계약 테스트**: Feign 클라이언트 인터페이스

### 테스트 데이터
- **H2**: 일부 서비스의 인메모리 DB (test 프로필)
- **Testcontainers**: 통합 테스트를 위한 PostgreSQL, Kafka
- **Fixtures**: 재사용 가능한 테스트 데이터 빌더

---

## 🚀 배포 & 운영

### 환경
- **dev**: 로컬 개발 (Config Server: localhost:18080)
- **prod**: Docker Compose → AWS ECS
- **test**: CI/CD 테스팅 환경

### 모니터링
- **Prometheus**: `http://localhost:9090` (메트릭)
- **Grafana**: `http://localhost:3000` (대시보드)
- **Zipkin**: `http://localhost:9411` (분산 추적)
- **Kafka UI**: `http://localhost:8080` (토픽 모니터링)

---

## ⚠️ 알려진 제약사항 & 해결책

### 분산 락 (Redisson)
- **위치**: Product 서비스, 재고 감소
- **이유**: 높은 트래픽에서 경합 조건 방지
- **패턴**: `product:{productId}:inventory`에 락

### JWT 전파
- **문제**: 인증 헤더가 서비스로 전달되지 않음
- **해결책**: Gateway 필터가 JWT를 다운스트림으로 복사

### WebSocket 인증
- **문제**: STOMP 인터셉터가 불안정함
- **해결책**: 커스텀 `HandshakeInterceptor` 사용

### Kafka 멱등성
- **문제**: 중복 메시지 처리
- **해결책**: 이벤트 ID를 통한 컨슈머 측 중복 제거

---

## 📋 이 리포지터리에서 작업하기

### 작업 시작 전
1. `docs/design-docs/`에서 관련 문서 읽기
2. `docs/exec-plans/active/`에서 진행 중인 작업 확인
3. 서비스의 기존 코드 구조 검토

### 작업 중
1. 레이어드 아키텍처 준수 (테스트로 강제됨)
2. 새로운 기능에 대한 테스트 추가
3. 동작이 변경되면 관련 문서 업데이트
4. 기존 패턴 사용 (코드베이스에서 예제 검색)

### 작업 후
1. 모든 테스트 통과 확인: `./gradlew test`
2. 빌드 성공 확인: `./gradlew build`
3. 해당되는 경우 품질 점수 업데이트
4. design-docs에 새로운 패턴 문서화

---

## 📚 심화 학습

포괄적인 정보는 다음을 참조하세요:

- **[ARCHITECTURE.md](ARCHITECTURE.md)** - 전체 시스템 설계
- **[docs/design-docs/](docs/design-docs/)** - 상세 설계 결정
- **[docs/MSA_PATTERNS.md](docs/MSA_PATTERNS.md)** - 서비스 통신 패턴
- **[docs/KAFKA_EVENTS.md](docs/KAFKA_EVENTS.md)** - 완전한 이벤트 카탈로그
- **[docs/DATABASE.md](docs/DATABASE.md)** - 데이터베이스 스키마 및 마이그레이션

---

## 🤝 도움 받기

**문서가 명확하지 않나요?** → 업데이트하세요! 에이전트와 사람 모두에게 도움이 됩니다.

**패턴이 확립되지 않았나요?** → `docs/design-docs/`에 설계 문서를 만드세요.

**품질 문제가 있나요?** → `docs/quality/QUALITY_SCORE.md`에서 추적하세요.

---

**기억하세요**: 이 리포지터리가 진실의 원천입니다. 에이전트가 알아야 할 모든 것은 여기에 버전 관리되고 발견 가능해야 합니다.

---

## 🚀 빠른 시작 가이드

### 프로젝트 개요

Live Commerce Platform은 **MSA (마이크로서비스 아키텍처)** 기반의 라이브 커머스 플랫폼입니다.
- **기술 스택**: Spring Boot 3.4.4, Java 17
- **개발 철학**: 에이전트 우선 개발, 사람이 조율하고 AI가 수행

### 주요 서비스

| 서비스 | 포트 | 역할 |
|---------|------|------|
| **Gateway** | 19091 | API 게이트웨이 |
| **User** | 19120 | 사용자 관리 |
| **Product** | 19070 | 상품 및 재고 관리 |
| **Order** | 19030 | 주문 관리 |
| **Payment** | 19080 | 결제 처리 |
| **LiveBroadcast** | 19060 | 라이브 방송 |
| **Chat** | 19040 | 실시간 채팅 |
| **AI** | 19050 | 채팅 요약 (Gemini API) |
| **Coupon** | 19100 | 쿠폰 관리 |
| **Notification** | 19110 | 알림 서비스 |
| **Company** | 19020 | 업체 관리 |
| **Eureka** | 19090 | 서비스 디스커버리 |
| **Config** | 18080 | 설정 서버 |

### 인프라 서비스

| 서비스 | 포트 | 용도 |
|---------|------|------|
| **PostgreSQL** | 5432 | 메인 데이터베이스 |
| **Redis** | 6379 | 캐시, 세션, 분산 락 |
| **Kafka** | 9092 | 이벤트 스트리밍 |
| **Kafka UI** | 8080 | Kafka 모니터링 |
| **Prometheus** | 9090 | 메트릭 수집 |
| **Grafana** | 3000 | 메트릭 시각화 |
| **Zipkin** | 9411 | 분산 추적 |

---

## 🔑 핵심 기술

- **Database**: PostgreSQL 15 + JPA/Hibernate
- **Cache**: Redis + Spring Session
- **분산 락**: Redisson (재고 동시성 제어)
- **메시징**: Apache Kafka 3.3.4
- **쿼리**: QueryDSL 5.0 (타입 안전 쿼리)
- **API 문서화**: Swagger/OpenAPI 3.0
- **모니터링**: Prometheus + Grafana
- **추적**: Zipkin 분산 추적
- **인증**: JWT 토큰, Spring Security
- **외부 API**: KakaoPay, Gemini, Slack, Google SMTP

---

## 📖 개발 참고사항

### QueryDSL 설정
- QueryDSL 사용 서비스: `order`, `product` 등
- Q-클래스는 `src/main/generated`에 생성됨
- 엔티티 변경 시 `./gradlew clean`으로 정리

### 분산 락 (Redisson)
- `product` 서비스의 재고 감소 작업에 사용
- 높은 트래픽에서 재고 차감 경합 조건 방지
- 락 키 패턴: 제품 ID 기반

### Kafka 토픽
일반적인 토픽:
- 쿠폰 발급 이벤트
- 주문 생성/업데이트 이벤트
- 결제 성공/실패 이벤트
- 재고 증가/감소 이벤트
- 알림 트리거 이벤트

### Gateway 인증
- Gateway 필터가 JWT 인증 헤더를 다운스트림 서비스로 복사
- JWT 시크릿은 `.env` 또는 config 서버에 설정
- 사용자 컨텍스트는 헤더를 통해 전파됨

### WebSocket (Chat 서비스)
- WebSocket 인증을 위한 커스텀 `HandshakeInterceptor` 사용
- STOMP 미사용, 커스텀 WebSocket 구현
- 채팅 메시지는 AI 서비스(Gemini API)를 통해 분석/요약 가능

### 설정 프로필
- `dev`: 로컬 개발
- `prod`: 프로덕션 (Docker/AWS)
- `test`: 테스팅 환경

---

## 🧪 테스팅

- **단위 테스트**: JUnit 5와 `@Test` 사용
- **통합 테스트**: `@SpringBootTest` 사용
- **Kafka 테스트**: `spring-kafka-test` 의존성 사용
- **DB 테스트**: 일부 서비스에서 H2 인메모리 데이터베이스 사용

**커버리지 요구사항**:
- 최소: 70% (미달 시 빌드 실패)
- 목표: 80%
- 중요 경로: 100% (결제, 재고, 인증)

---

## 🔍 API 테스팅

Swagger UI를 사용한 API 문서화 및 테스팅:
- Gateway Swagger: `http://localhost:19091/swagger-ui.html`
- 개별 서비스 Swagger: `http://localhost:<port>/swagger-ui.html`

회원가입 요청 예시:
```bash
POST http://localhost:19091/v2/auth/signup
{
  "email": "test@example.com",
  "password": "12345678"
}
```

---

## ⚡ 알려진 문제 & 해결책

팀의 트러블슈팅 경험:

1. **서비스 간 JWT 미전파**: Gateway 필터가 인증 헤더를 복사하도록 보장
2. **WebSocket 인증 문제**: STOMP 인터셉터 대신 커스텀 `HandshakeInterceptor` 사용
3. **Kafka 중복 메시지 처리**: 컨슈머에서 멱등성 키 구현
4. **Docker 네트워크 오류**: 모든 서비스가 동일한 Docker 네트워크(`8do-network`) 사용 확인

---

**마지막 업데이트**: 2026-04-23
**관리**: Engineering Team
