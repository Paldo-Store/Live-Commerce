# Design Docs Index

**Central registry of all architectural decisions and design patterns**

---

## Core Beliefs

These principles guide all technical decisions:

1. **Agent-First Development**: Optimize for agent readability and autonomous operation
2. **Documentation as Code**: Everything documented, versioned, and discoverable
3. **Mechanical Enforcement**: Architecture rules enforced by tests and linters, not just code reviews
4. **Fast Feedback Loops**: Agents can build, test, verify locally without waiting for CI
5. **Eventual Consistency Over Distributed Transactions**: Accept eventual consistency for scalability
6. **Boring Technology**: Prefer stable, well-documented technologies over cutting-edge
7. **Service Independence**: Each service can be developed, deployed, scaled independently

---

## Architecture Decisions

### Active Design Docs

| Title | Status | Created | Owner | Summary |
|-------|--------|---------|-------|---------|
| [Layered Architecture](layered-architecture.md) | ✅ Active | 2025-08 | Team | Mandatory 4-layer structure per service |
| [Event-Driven Communication](event-driven-kafka.md) | ✅ Active | 2025-08 | Team | Kafka for async, Feign for sync |
| [Distributed Lock Strategy](distributed-lock.md) | ✅ Active | 2025-09 | Team | Redisson for inventory/coupon concurrency |
| [API Gateway Pattern](api-gateway.md) | ✅ Active | 2025-08 | Team | Single entry point via Spring Cloud Gateway |

### Proposed

| Title | Status | Proposed | Summary |
|-------|--------|----------|---------|
| [Circuit Breaker Implementation](circuit-breaker-proposal.md) | 🔄 RFC | 2026-04 | Resilience4j for Feign clients |
| [Database Per Service Migration](db-separation.md) | 🔄 RFC | 2026-04 | Move from shared DB to per-service DBs |
| [Event Sourcing for Orders](event-sourcing-orders.md) | 💡 Draft | 2026-04 | Full audit trail via event sourcing |

### Deprecated

| Title | Deprecated | Reason |
|-------|------------|--------|
| ~~STOMP for WebSocket~~ | 2025-09 | Replaced with custom HandshakeInterceptor |

---

## Design Patterns in Use

### Service Communication

- **[Saga Pattern](../MSA_PATTERNS.md#saga-pattern-event-driven)**: Distributed transactions via Kafka events
- **[API Gateway](../MSA_PATTERNS.md#api-gateway-pattern)**: Centralized routing and auth
- **[Service Discovery](../MSA_PATTERNS.md#service-discovery-pattern)**: Eureka for dynamic service location

### Data Management

- **[Cache-Aside](../MSA_PATTERNS.md#cache-aside-pattern)**: Redis caching for product data
- **[Distributed Lock](../MSA_PATTERNS.md#distributed-lock-pattern)**: Redisson for inventory operations
- **Database Per Service** (in progress): Logical separation, physical migration planned

### Resilience

- **Retry Logic**: Kafka consumer retries with exponential backoff
- **Idempotency**: All event consumers check for duplicate processing
- **Circuit Breaker** (planned): Resilience4j for Feign calls

---

## Technology Choices

### Core Stack

| Technology | Version | Rationale |
|------------|---------|-----------|
| **Java** | 17 | LTS, modern features (records, sealed classes) |
| **Spring Boot** | 3.4.4 | Industry standard, excellent MSA support |
| **Gradle** | 8.x | Better performance than Maven, Kotlin DSL |
| **PostgreSQL** | 15 | Reliable, ACID, good JSON support |
| **Redis** | Latest | Fast, versatile (cache, session, lock) |
| **Kafka** | 3.3.4 | Industry-standard event streaming |

### Rationale: "Boring Technology"

We intentionally choose **proven, stable technologies** because:
- Agents work better with well-documented tools
- Fewer surprises, more predictable behavior
- Large community, extensive examples
- Easier to hire/onboard developers

**Trade-off**: We sacrifice "cutting-edge" for reliability and discoverability.

---

## Testing Strategy

### Test Pyramid

```
        ┌─────┐
        │ E2E │ (5% - Manual/exploratory)
        └─────┘
      ┌─────────┐
      │Integration│ (25% - Testcontainers, Kafka)
      └─────────┘
    ┌─────────────┐
    │    Unit     │ (70% - JUnit, Mockito)
    └─────────────┘
```

### Coverage Requirements

- **Minimum**: 70% (build fails below)
- **Target**: 80%
- **Critical paths**: 100% (payment, inventory, auth)

### Testing Principles

1. **Arrange-Act-Assert**: Clear test structure
2. **Test Isolation**: No shared state between tests
3. **Fast Tests**: Unit tests < 100ms, integration < 5s
4. **Meaningful Names**: `testMethodName_scenario_expectedResult`

---

## Security Considerations

### Authentication & Authorization

- **JWT Tokens**: Stateless authentication
- **Gateway Validation**: Centralized JWT verification
- **Service-to-Service**: Trust within cluster (future: mTLS)

### Input Validation

- **DTO Validation**: `@Valid` on all request DTOs
- **Sanitization**: Prevent SQL injection, XSS
- **Rate Limiting** (planned): Gateway-level throttling

### Secrets Management

- **Current**: `.env` files (gitignored)
- **Prod**: AWS Secrets Manager / Parameter Store
- **Never**: Hard-coded credentials

---

## Observability

### Three Pillars

1. **Logs**: Structured logging (SLF4J + Logback)
2. **Metrics**: Prometheus + Grafana dashboards
3. **Traces**: Zipkin distributed tracing

### What to Monitor

- **RED Metrics**: Rate, Errors, Duration
- **Business Metrics**: Orders/hour, inventory low stock alerts
- **Infrastructure**: CPU, memory, DB connections

### Alerting

- **Critical**: Payment failures, DB down, Kafka lag > 1000
- **Warning**: Slow queries (>2s), cache hit rate < 70%

---

## Code Style & Conventions

### Naming Conventions

- **Classes**: `PascalCase` (e.g., `OrderService`)
- **Methods**: `camelCase` (e.g., `createOrder`)
- **Constants**: `UPPER_SNAKE_CASE` (e.g., `MAX_RETRY_ATTEMPTS`)
- **Packages**: `lowercase` (e.g., `com.live_commerce.order`)

### File Organization

```
service/
├── presentation/controller/   # REST endpoints
├── application/
│   ├── dto/                  # Request/Response DTOs
│   ├── service/              # Business logic
│   └── mapper/               # Entity ↔ DTO
├── domain/
│   ├── model/                # JPA entities
│   └── repository/           # Data access
└── infrastructure/
    ├── config/               # Spring configs
    └── client/               # Feign clients
```

### Lombok Usage

**Encouraged**:
- `@Getter`, `@Setter` on entities/DTOs
- `@Builder` for complex object creation
- `@RequiredArgsConstructor` for DI

**Discouraged**:
- `@Data` (too implicit, hides behavior)
- `@EqualsAndHashCode` on JPA entities (use business keys)

---

## Evolution Process

### Proposing New Patterns

1. Create design doc in `docs/design-docs/`
2. Include: Problem, Options, Recommendation, Trade-offs
3. Get team review (async via PR)
4. Mark as "RFC" in index.md
5. After consensus, mark "Active"

### Updating Existing Patterns

1. Edit existing design doc
2. Add "Updated" section with date and reason
3. Update affected code incrementally
4. Update ARCHITECTURE.md if needed

### Deprecating Patterns

1. Mark as "Deprecated" in index.md
2. Add deprecation reason
3. Create migration guide if needed
4. Remove old code over time (not immediately)

---

## References

- **[ARCHITECTURE.md](../../ARCHITECTURE.md)** - High-level system design
- **[MSA_PATTERNS.md](../MSA_PATTERNS.md)** - Pattern catalog
- **[KAFKA_EVENTS.md](../KAFKA_EVENTS.md)** - Event specifications
- **[QUALITY_SCORE.md](../quality/QUALITY_SCORE.md)** - Quality tracking

---

**Last Updated**: 2026-04-23
**Maintained By**: Engineering Team
