# AGENTS.md

**Entry point for AI agents working in this repository.**

This file serves as a **map** to guide agents through the Live Commerce Platform codebase. Think of it as a table of contents, not an encyclopedia.

---

## 🎯 Core Philosophy

**Agent-First Development**: This codebase is optimized for agent readability and autonomous operation.

- **Everything is in the repository**: If it's not versioned in this repo, it doesn't exist for agents
- **Documentation is the source of truth**: Code should reflect what's documented
- **Architecture is enforced mechanically**: Through tests, linters, and CI
- **Fast feedback loops**: Agents can build, test, and verify locally

---

## 📍 Navigation Map

### Start Here
- **[ARCHITECTURE.md](ARCHITECTURE.md)** - System architecture, service boundaries, and layering rules
- **[CLAUDE.md](CLAUDE.md)** - Claude Code specific guidance and commands

### Core Documentation
All documentation lives in `docs/` and is structured for agent discoverability:

```
docs/
├── design-docs/        # Architecture decisions and patterns
├── product-specs/      # Product requirements and user flows
├── exec-plans/         # Current and completed execution plans
├── references/         # External API docs and library guides
└── quality/           # Quality metrics and improvement tracking
```

### Key Files
- **[docs/DESIGN.md](docs/DESIGN.md)** - Design principles and engineering standards
- **[docs/MSA_PATTERNS.md](docs/MSA_PATTERNS.md)** - Microservices communication patterns
- **[docs/KAFKA_EVENTS.md](docs/KAFKA_EVENTS.md)** - Event-driven architecture guide
- **[docs/quality/QUALITY_SCORE.md](docs/quality/QUALITY_SCORE.md)** - Quality tracking by service

---

## 🏗️ Architecture at a Glance

**MSA (Microservices Architecture)** with 13 independent services:

### Service Categories
- **Infrastructure**: gateway, eureka, config
- **Business**: user, product, order, payment, company, coupon
- **Real-time**: livebroadcast, chat, ai
- **Cross-cutting**: notification

### Communication
- **Sync**: Feign Client (REST)
- **Async**: Kafka (events)
- **Discovery**: Eureka
- **Gateway**: All requests → port 19091

**Full details**: [ARCHITECTURE.md](ARCHITECTURE.md)

---

## 📦 Service Structure (Layered Architecture)

Each service follows this **mandatory** structure:

```
src/main/java/com/live_commerce/<service>/
├── presentation/       # Controllers, REST endpoints
├── application/        # DTOs, Services, Business logic
├── domain/            # Entities, Repositories (JPA)
└── infrastructure/    # Config, Security, External clients
```

**Rules**:
- Dependencies flow **downward only**: presentation → application → domain
- No circular dependencies
- Cross-cutting concerns via infrastructure layer

**Enforcement**: Structural tests in each service validate layering

---

## 🔧 Common Development Tasks

### Build & Test
```bash
./gradlew build                    # Build all services
./gradlew :order:build            # Build specific service
./gradlew test                    # Run all tests
./gradlew :product:test           # Test specific service
```

### Run Locally
```bash
docker-compose up -d              # Start infrastructure (DB, Kafka, Redis)
cd <service> && ./gradlew bootRun # Run service locally
```

### QueryDSL (for services using it)
```bash
./gradlew clean                   # Clean generated Q-classes
./gradlew build                   # Regenerate Q-classes
```

---

## 🎨 Code Style & Standards

### Enforced via Tools
- **Lombok**: Reduce boilerplate (@Getter, @Builder, etc.)
- **QueryDSL**: Type-safe queries (order, product services)
- **Swagger**: API documentation (auto-generated)
- **JUnit 5**: Testing framework

### Conventions
- **DTOs**: Separate Request/Response in `application/dto/`
- **Naming**: `<Entity><Action><Type>` (e.g., `OrderCreateRequest`)
- **Exceptions**: Custom exception codes per service
- **Logging**: Structured logging (SLF4J)

---

## 🔄 Event-Driven Patterns (Kafka)

**Key Event Flows**:

1. **Order → Product**: Order created → Inventory decrease
2. **Order → Coupon**: Order created → Coupon usage
3. **Payment → Order**: Payment success/fail → Order status update
4. **User → Coupon**: User signup → Welcome coupon issued
5. **Broadcast → Notification**: Broadcast starts → Notify subscribers

**Full event catalog**: [docs/KAFKA_EVENTS.md](docs/KAFKA_EVENTS.md)

---

## 🧪 Testing Philosophy

### Test Pyramid
- **Unit tests**: Business logic in `application/service/`
- **Integration tests**: `@SpringBootTest` with test DB
- **Contract tests**: Feign client interfaces

### Test Data
- **H2**: In-memory DB for some services (test profile)
- **Testcontainers**: PostgreSQL, Kafka for integration tests
- **Fixtures**: Reusable test data builders

---

## 🚀 Deployment & Operations

### Environments
- **dev**: Local development (Config Server: localhost:18080)
- **prod**: Docker Compose → AWS ECS
- **test**: CI/CD testing environment

### Monitoring
- **Prometheus**: `http://localhost:9090` (metrics)
- **Grafana**: `http://localhost:3000` (dashboards)
- **Zipkin**: `http://localhost:9411` (distributed tracing)
- **Kafka UI**: `http://localhost:8080` (topic monitoring)

---

## ⚠️ Known Constraints & Solutions

### Distributed Lock (Redisson)
- **Where**: Product service, inventory decrease
- **Why**: Prevent race conditions in high traffic
- **Pattern**: Lock on `product:{productId}:inventory`

### JWT Propagation
- **Issue**: Auth headers not forwarding to services
- **Solution**: Gateway filter copies JWT to downstream

### WebSocket Auth
- **Issue**: STOMP interceptors unreliable
- **Solution**: Custom `HandshakeInterceptor`

### Kafka Idempotency
- **Issue**: Duplicate message processing
- **Solution**: Consumer-side deduplication via event ID

---

## 📋 Working with This Repo

### Before Starting Work
1. Read relevant docs in `docs/design-docs/`
2. Check `docs/exec-plans/active/` for ongoing work
3. Review service's existing code structure

### During Work
1. Follow layered architecture (enforced by tests)
2. Add tests for new functionality
3. Update relevant docs if behavior changes
4. Use existing patterns (search codebase for examples)

### After Work
1. Ensure all tests pass: `./gradlew test`
2. Build successfully: `./gradlew build`
3. Update quality score if applicable
4. Document any new patterns in design-docs

---

## 📚 Deep Dives

For comprehensive information, see:

- **[ARCHITECTURE.md](ARCHITECTURE.md)** - Full system design
- **[docs/design-docs/](docs/design-docs/)** - Detailed design decisions
- **[docs/MSA_PATTERNS.md](docs/MSA_PATTERNS.md)** - Service communication patterns
- **[docs/KAFKA_EVENTS.md](docs/KAFKA_EVENTS.md)** - Complete event catalog
- **[docs/DATABASE.md](docs/DATABASE.md)** - Database schema and migrations

---

## 🤝 Getting Help

**Documentation not clear?** → Update it! Agents and humans both benefit.

**Pattern not established?** → Create a design doc in `docs/design-docs/`

**Quality issues?** → Track in `docs/quality/QUALITY_SCORE.md`

---

**Remember**: This repository is the source of truth. Everything agents need to know should be here, versioned, and discoverable.
