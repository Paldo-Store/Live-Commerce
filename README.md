# 🎥 Live Commerce Platform

라이브 방송을 통해 실시간 상품 판매가 가능한 **MSA 기반의 라이브 커머스 플랫폼**입니다.

방송, 채팅, 상품 주문, 쿠폰 발급, 결제, 알림 등 다양한 기능을 마이크로서비스로 분리하여 확장성과 유지보수성을 고려하여 설계했습니다.

<br/>

## 📌 목차

- [🧩 프로젝트 개요]
- [🎯 서비스/프로젝트 목표]
- [🚀 주요 기능]
- [⚙️ 기술 스택]
- [🧱 아키텍처 구성]
- [📁 디렉토리 구조]
- [🛠️ 설치 및 실행 방법]
- [🧪 테스트 방법]
- [📈 트러블슈팅 & 의사소통]
- [🙋 팀원 소개]
- [📄 라이선스]

<br/>

  ## 🧩 프로젝트 개요

> 실시간 라이브 방송을 통해 상품을 소개하고, 시청자와의 실시간 소통 및 즉시 구매가 가능한 커머스 플랫폼을 개발했습니다.
> 
- **개발 기간:** 2025.04.03 ~ 2025.05.1
- **개발 인원:** BE 4명
- **형태:** MSA 기반 분산 서비스

<br/>

## 🎯 서비스/프로젝트 목표

### 📦 MSA 아키텍처 기반 서비스 분리 및 독립성 확보

> 목적: 시스템 확장성과 유지보수성을 확보
> 
- 주문, 상품, 방송, 쿠폰 등 **도메인별 서비스 분리**
- **서비스 독립 배포** 가능
- **무중단 확장** 대응 구조 설계

</br>

### 📩 Kafka 기반 이벤트 비동기 처리

> 목적: 서비스 간 느슨한 결합과 이벤트 기반 처리 구현
> 
- **쿠폰 발급, 재고 감소/복원, 주문/결제 성공/실패** 처리에 Kafka 적용
- 주문 흐름 중 발생하는 상태 변화를 Kafka 이벤트로 비동기 전달

</br>

### 🔒 동시성 고려 재고 락 적용

> 목적: 분산 환경에서도 데이터 정합성 보장
> 
- 재고 감소 시 **Redisson 분산락** 적용하여 **중복 차감 방지**

</br>

### 💬 WebSocket 기반 실시간 채팅 + AI 요약 분석

> 목적: 방송 중 사용자 피드백 및 쇼호스트 지원 강화
> 
- **WebSocket**을 이용해 실시간 채팅 기능 구현
- **Gemini API**를 연동하여 채팅 요약 및 분석 데이터 제공
- 쇼호스트에게 **실시간 고객 반응 요약** 피드백 전달

</br>

### 📈 Prometheus + Grafana 기반 모니터링

> 목적: 실시간 서비스 상태 및 주요 지표 시각화
> 
- 서비스 주요 지표를 **Prometheus**로 수집
- **Grafana** 대시보드를 통해 시스템 상태를 실시간 모니터링
- 이상 징후 발생 시 빠른 탐지 가능

</br>

### 📚 Swagger 기반 명확한 API 명세 관리

> 목적: 프론트엔드 및 외부 연동을 위한 인터페이스 표준화
> 
- **Swagger UI**를 통해 API 스펙 명확히 제공
- 개발 초기부터 **Contract-First** 개발 방식 적용

</br>

---

 ## 🚀주요 기능

### 🎥 실시간 방송 관리 (Live Broadcast)

📎 **기능 요약**

- 라이브 방송 예약/시작/종료 관리
- 방송 구독자에게 방송 시작 10분 전 Kafka 기반 알림 트리거 발송

<br/>

### 🛍 상품 관리 & 재고 차감 및 복구

📎 **기능 요약**

- 상품 조회 시 Redis를 활용하여 캐싱
- 판매 인기 상품 Top10 랭킹
- Kafka 이벤트 기반 재고 감소/복원 비동기 처리
- Redisson 락 적용 → 트래픽 급증에도 데이터 정합성 보장

<br/>

### 🎟 쿠폰 발급 및 사용

📎 **기능 요약**

- 회원가입 시 Kafka 이벤트로 쿠폰 자동 발급
- 주문 생성 시 쿠폰 사용 여부를 Kafka 비동기로 처리
- Redis TTL 기반 쿠폰 만료/사용 상태 관리

<br/>

### 💬 실시간 채팅 + AI 채팅 요약 분석

📎 **기능 요약**

- WebSocket 기반 실시간 채팅 시스템 구현
- Gemini API 연동하여 실시간 채팅 요약 및 분석 제공

<br/>

### 🧾 주문 생성 및 상태 전이 비동기 처리

📎 **기능 요약**

- 주문 생성 시 Kafka를 통해 재고 차감 및 쿠폰 사용 비동기화
- 결제 성공/실패에 따른 주문/결제 상태를 Kafka 이벤트로 관리
- Feign 요청을 통해 MSA 모듈 간 통신
- 주문 상태 변경을 통한 수정 및 결제 처리 가능

<br/>

---

### ⚙️ 기술 스택

| 분류           | 기술                                                                 |
|----------------|----------------------------------------------------------------------|
| Language       | Java 17                                                              |
| Backend        | Spring Boot 3.4.4, Spring Data JPA, Spring Security, Spring Webflux, QueryDSL 5.0 |
| Messaging      | Apache Kafka 3.3.4                                                   |
| Database       | PostgreSQL 15, Redis (Redisson 3.45.1)                               |
| External API   | KakaoPay, Gemini (Google), Slack, Google SMTP                        |
| Monitoring     | Prometheus, Grafana                                                  |
| Infra          | Docker, Docker-Compose, AWS ECS, ECR, RDS                            |
| CI/CD          | GitHub Actions                                                       |
| Testing        | JMeter, JUnit 5                                                      |
| Tools          | Swagger, GitHub, Jira, Notion                                        |

<br/>

---

### ⚙️ 패키지 구조

```
[Gateway] → [Auth Service]
          → [User Service]
          → [Broadcast Service] → [Chat Service]
          → [Product Service] → [Order Service] → [Payment Service]
          → [Coupon Service]
          → [Notification Service]

```
          
- 각 서비스는 독립적으로 배포 가능
- Kafka를 통해 비동기 이벤트 처리
- WebSocket은 채팅 서비스에서 관리

<br/>

---
### 📁 인프라 설계도


---


### 📁 ERD설계도

![8TEAM_ERD (1)](https://github.com/user-attachments/assets/bc7c4911-daf8-45ee-8795-e5d9fd59f6b5)

---

### 📁 디렉토리 구조

```📦live-commerce
📂service
 ┣ 📂ai
 ┣ 📂chat
 ┣ 📂company
 ┣ 📂coupon
 ┣ 📂livebroadcast
 ┣ 📂notification
 ┣ 📂order
 ┣ 📂product
 ┣ 📂payment
 ┗ 📂user
📂server
 ┣ 📂config
 ┣ 📂eureka
 ┣ 📂gateway
```

<br/>

### 🛠️ 설치 및 실행 방법

1. 레포지토리 클론

```
git clone <https://github.com/Paldo-Store/Live-Commerce>
cd live-commerce
```

2. Docker로 전체 서비스 실행

```
docker-compose up --build
```
3. API 테스트 (예: 회원가입)

```
POST http:localhost:19091/v2/auth/signup
{
  "email": "test@example.com",
  "password": "12345678"
}
```

---

### 🧪 테스트 방법

> Postman 또는 Thunder Client를 통한 API 테스트

- 단위 테스트: ./gradlew test

- 통합 테스트: @SpringBootTest 사용

---

### 📈 트러블슈팅 & 고민점

> 문제 상황	해결 방법

- JWT 인증 정보가 서비스 간 전달되지 않음	Gateway 필터에서 인증 헤더를 복사
- WebSocket 사용자 인증 문제	STOMP 대신 Custom HandshakeInterceptor 사용
- Kafka 메시지 중복 처리	Consumer에서 중복 Key 체크로 보완
- 도커 네트워크 연결 오류	각 서비스에 동일한 Docker 네트워크 설정

---

## 🙋 팀원 소개

| 이름       | 역할           | 담당 서비스                       | GitHub                                      | 
|------------|----------------|------------------------------------|---------------------------------------------|
| 서태웅 | 🛠 백엔드(팀장)   | 결제(Payment), AI         | [@dev-kim](https://github.com/STW5)       | 
| 김민주  | 🎨 백엔드(부팀장) | 방송(Broadcast), 상품(Product) |  [@minju-kim](https://github.com/mjjmjmjmj) | 
| 정아현  | 📡 백엔드(문서담당)     | 주문(order) ,업체(Conpany),  채팅(Chat)         | [@greenblueredgreen](https://github.com/greenblueredgreen) | 
| 최호진| 📦 백엔드(테크리더)      | 쿠폰(Coupon), 알림(Notification)    | [@gentle-tiger](https://github.com/gentle-tiger) |
