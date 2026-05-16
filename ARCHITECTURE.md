# ARCHITECTURE.md

**System Architecture - Live Commerce Platform**

This document defines the high-level architecture, service boundaries, communication patterns, and architectural constraints of the Live Commerce Platform.

---

## Table of Contents

1. [System Overview](#system-overview)
2. [Service Catalog](#service-catalog)
3. [Layered Architecture (Per Service)](#layered-architecture-per-service)
4. [Communication Patterns](#communication-patterns)
5. [Data Flow](#data-flow)
6. [Infrastructure](#infrastructure)
7. [Architectural Constraints](#architectural-constraints)

---

## System Overview

**Architecture Style**: Microservices Architecture (MSA)

**Core Principles**:
- **Service Independence**: Each service can be deployed independently
- **Event-Driven**: Async communication via Kafka for loose coupling
- **API Gateway Pattern**: Single entry point for all client requests
- **Service Discovery**: Dynamic service registration via Eureka
- **Observability**: Centralized monitoring, logging, and tracing

### High-Level Diagram

```
                           ┌─────────────┐
                           │   Clients   │
                           └──────┬──────┘
                                  │
                           ┌──────▼──────┐
                           │   Gateway   │ :19091
                           └──────┬──────┘
                                  │
                    ┌─────────────┼─────────────┐
                    │    Eureka (Discovery)     │ :19090
                    └─────────────┬─────────────┘
                                  │
        ┌─────────────────────────┼─────────────────────────┐
        │                         │                         │
   ┌────▼────┐              ┌────▼────┐              ┌────▼────┐
   │  User   │              │ Product │              │  Order  │
   │ Service │◄────Kafka────┤ Service │◄────Kafka────┤ Service │
   └────┬────┘              └────┬────┘              └────┬────┘
        │                         │                         │
        │                    ┌────▼────┐              ┌────▼────┐
        │                    │Broadcast│              │ Payment │
        │                    │ Service │              │ Service │
        │                    └────┬────┘              └─────────┘
        │                         │
   ┌────▼────┐              ┌────▼────┐
   │ Coupon  │              │  Chat   │◄────┐
   │ Service │              │ Service │     │
   └─────────┘              └────┬────┘     │
                                 │     ┌────▼────┐
                            ┌────▼────┐│   AI    │
                            │ Notif.  ││ Service │
                            │ Service │└─────────┘
                            └─────────┘

        Infrastructure Layer:
        ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐
        │PostgreSQL│  │  Redis   │  │  Kafka   │  │Prometheus│
        └──────────┘  └──────────┘  └──────────┘  └──────────┘
```

---

## Service Catalog

### Infrastructure Services

| Service | Port | Purpose | Technology |
|---------|------|---------|------------|
| **Gateway** | 19091 | API Gateway, routing, JWT verification | Spring Cloud Gateway (WebFlux) |
| **Eureka** | 19090 | Service discovery and registration | Spring Cloud Netflix Eureka |
| **Config** | 18080 | Centralized configuration management | Spring Cloud Config |

### Business Services

| Service | Port | Domain | Key Responsibilities |
|---------|------|--------|---------------------|
| **User** | 19120 | User Management | Authentication, authorization, user profiles |
| **Product** | 19070 | Product Catalog | Product CRUD, inventory management, stock control |
| **Order** | 19030 | Order Management | Order creation, status tracking, order history |
| **Payment** | 19080 | Payment Processing | KakaoPay integration, payment verification |
| **Company** | 19020 | Seller Management | Company registration, seller profiles |
| **Coupon** | 19100 | Promotion | Coupon issuance, usage, expiration management |

### Real-Time Services

| Service | Port | Domain | Key Responsibilities |
|---------|------|--------|---------------------|
| **LiveBroadcast** | 19060 | Live Streaming | Broadcast scheduling, start/stop, viewer management |
| **Chat** | 19040 | Real-Time Chat | WebSocket connections, message broadcasting |
| **AI** | 19050 | Chat Analysis | Gemini API integration, chat summarization |

### Cross-Cutting Services

| Service | Port | Domain | Key Responsibilities |
|---------|------|--------|---------------------|
| **Notification** | 19110 | Notifications | Email, Slack notifications, event-driven alerts |

---

## Layered Architecture (Per Service)

Each microservice **MUST** follow this layered structure:

```
src/main/java/com/live_commerce/<service-name>/
│
├── presentation/              # Presentation Layer
│   └── controller/           # REST controllers, request/response handling
│
├── application/              # Application Layer
│   ├── dto/                 # Data Transfer Objects
│   │   ├── request/         # Incoming request DTOs
│   │   └── response/        # Outgoing response DTOs
│   ├── service/             # Business logic orchestration
│   └── mapper/              # Entity ↔ DTO conversion
│
├── domain/                   # Domain Layer
│   ├── model/               # JPA entities (domain models)
│   └── repository/          # Data access interfaces
│
└── infrastructure/           # Infrastructure Layer
    ├── config/              # Spring configurations
    ├── security/            # Security configs, JWT handling
    ├── client/              # Feign clients for external services
    └── kafka/               # Kafka producers/consumers
```

### Dependency Rules (ENFORCED)

**Direction**: `presentation → application → domain`

```
┌───────────────┐
│ Presentation  │  (Controllers)
└───────┬───────┘
        │ depends on
        ▼
┌───────────────┐
│  Application  │  (Services, DTOs, Mappers)
└───────┬───────┘
        │ depends on
        ▼
┌───────────────┐
│    Domain     │  (Entities, Repositories)
└───────────────┘
        ▲
        │ used by
┌───────┴───────┐
│Infrastructure │  (Config, Clients, Kafka)
└───────────────┘
```

**Rules**:
1. **Presentation** can depend on Application and Domain
2. **Application** can depend on Domain only
3. **Domain** has no dependencies on other layers (pure business logic)
4. **Infrastructure** can depend on all layers but others should not depend on it (dependency inversion)

**Violation Examples** (NOT ALLOWED):
- ❌ Domain importing from Application
- ❌ Domain importing from Presentation
- ❌ Application importing from Presentation

---

## Communication Patterns

### 1. Synchronous Communication (Feign Client)

**When to use**: Immediate response needed, query operations

**Example**: Order service fetching product details

```java
// In order service
@FeignClient(name = "product-service")
public interface ProductClient {
    @GetMapping("/api/products/{productId}")
    ProductResponse getProduct(@PathVariable Long productId);
}
```

**Services using Feign**:
- Order → Product (product details, inventory check)
- Order → Payment (payment initiation)
- Order → User (user validation)
- Broadcast → Notification (trigger notifications)

### 2. Asynchronous Communication (Kafka)

**When to use**: Event-driven workflows, eventual consistency, decoupling

**Kafka Topics** (see [docs/KAFKA_EVENTS.md](docs/KAFKA_EVENTS.md) for full catalog):

| Topic | Producer | Consumer | Purpose |
|-------|----------|----------|---------|
| `user.signup` | User | Coupon | Issue welcome coupon on signup |
| `order.created` | Order | Product, Coupon | Decrease inventory, apply coupon |
| `payment.success` | Payment | Order | Mark order as paid |
| `payment.failed` | Payment | Order, Product | Rollback inventory |
| `inventory.decreased` | Product | Order | Confirm stock deduction |
| `broadcast.scheduled` | Broadcast | Notification | Notify subscribers 10min before |

### 3. Gateway Routing

All client requests go through **Gateway** (port 19091):

```
Client Request → Gateway → Service Discovery (Eureka) → Target Service
```

**Gateway responsibilities**:
- Route requests to appropriate service
- JWT token validation
- Request/response logging
- Rate limiting (if configured)

---

## Data Flow

### Example: Order Creation Flow

```
1. Client → Gateway: POST /api/orders
2. Gateway → Order Service (via Eureka)
3. Order Service:
   a. Validate user (Feign → User Service)
   b. Check inventory (Feign → Product Service)
   c. Create order (save to DB)
   d. Publish "order.created" event (Kafka)
4. Product Service:
   a. Consume "order.created" event
   b. Decrease inventory (Redisson lock)
   c. Publish "inventory.decreased" event
5. Coupon Service:
   a. Consume "order.created" event
   b. Mark coupon as used
6. Order Service:
   a. Consume "inventory.decreased" event
   b. Proceed to payment
7. Order Service → Payment Service (Feign)
8. Payment Service → KakaoPay API
9. Payment Service:
   a. On success: Publish "payment.success"
   b. On failure: Publish "payment.failed"
10. Order Service:
    a. Consume "payment.success/failed"
    b. Update order status
```

**Key Pattern**: Saga pattern with eventual consistency

---

## Infrastructure

### Databases

**PostgreSQL** (port 5432):
- One logical database: `livecommerce`
- Schema separation per service (future: separate DBs)
- Initialized via `init-scripts/*.sql`

**Redis** (port 6379):
- Session management (Spring Session)
- Caching (product details, rankings)
- Distributed lock (Redisson) for inventory
- TTL-based expiration (coupons)

### Messaging

**Kafka** (port 9092):
- Zookeeper on port 2181
- Kafka UI on port 8080
- Topics created automatically by services

### Monitoring Stack

**Prometheus** (port 9090):
- Scrapes metrics from `/actuator/prometheus` endpoints
- Retention: 15 days (configurable)

**Grafana** (port 3000):
- Dashboards for service health
- Alert rules (via Alertmanager)

**Zipkin** (port 9411):
- Distributed tracing across services
- Trace correlation via `traceId`

---

## Architectural Constraints

### 1. Service Isolation

**Rule**: Each service has its own codebase, build, and deployment

**Enforcement**:
- Separate `build.gradle` per service
- Independent Docker containers
- No shared code libraries (除 common utilities)

### 2. Database Per Service (Future Goal)

**Current**: Shared PostgreSQL with schema separation
**Target**: Each service owns its database
**Migration Path**: Documented in `docs/design-docs/database-separation.md`

### 3. No Direct DB Access Across Services

**Rule**: Service A cannot query Service B's database

**Enforcement**:
- Use Feign for sync queries
- Use Kafka events for async updates
- Code reviews check for violations

### 4. Idempotent Event Consumers

**Rule**: Kafka consumers must handle duplicate messages

**Pattern**:
```java
@KafkaListener(topics = "order.created")
public void handleOrderCreated(OrderCreatedEvent event) {
    // Check if already processed
    if (processedEvents.contains(event.getId())) {
        log.info("Duplicate event ignored: {}", event.getId());
        return;
    }

    // Process event
    processOrder(event);

    // Mark as processed
    processedEvents.add(event.getId());
}
```

### 5. Circuit Breaker Pattern (Recommended)

**Tool**: Resilience4j (to be added)
**Use case**: Feign client calls
**Benefit**: Prevent cascade failures

---

## Technology Constraints

### Mandatory

- **Java 17**: Language version
- **Spring Boot 3.4.4**: Framework
- **Gradle**: Build tool
- **JPA/Hibernate**: ORM
- **Lombok**: Boilerplate reduction

### Preferred

- **QueryDSL**: Type-safe queries (use in complex query services)
- **Swagger/OpenAPI**: API documentation
- **JUnit 5**: Testing

### Allowed

- **H2**: In-memory DB for tests only
- **Mockito**: Mocking framework
- **Testcontainers**: Integration tests

### Forbidden

- **Direct JDBC**: Use JPA/QueryDSL instead
- **Embedded Config**: Use Config Server
- **Hard-coded URLs**: Use Eureka service names

---

## Evolution & Maintenance

### Adding New Service

1. Create service directory (use existing service as template)
2. Add to `settings.gradle`
3. Configure in `docker-compose.yml`
4. Register with Eureka
5. Document in this file and `docs/design-docs/`

### Changing Architecture

1. Create design doc in `docs/design-docs/`
2. Get team consensus
3. Update this ARCHITECTURE.md
4. Update affected service docs
5. Implement incrementally

### Quality Gates

- All services must pass structural tests
- Integration tests required for Kafka consumers
- API contracts documented in Swagger
- New patterns documented in design-docs

---

## References

- **[docs/MSA_PATTERNS.md](docs/MSA_PATTERNS.md)** - Common MSA patterns used
- **[docs/KAFKA_EVENTS.md](docs/KAFKA_EVENTS.md)** - Complete event catalog
- **[docs/DATABASE.md](docs/DATABASE.md)** - Database schema and conventions
- **[docs/design-docs/](docs/design-docs/)** - Detailed design decisions

---

**Last Updated**: 2026-04-23
**Maintained By**: Engineering Team
