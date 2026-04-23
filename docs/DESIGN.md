# Design Principles

**Core design principles for the Live Commerce Platform**

---

## Agent-First Development

**Principle**: Optimize for AI agent readability and autonomous operation

**In Practice**:
- All knowledge versioned in repository (no external docs)
- Documentation as source of truth (code reflects docs)
- Structured, discoverable documentation (not one giant file)
- Mechanical enforcement via linters and tests

**Rationale**: Agents work best with clear, structured, versioned information that's accessible during execution.

---

## Layered Architecture

**Principle**: Strict 4-layer structure per service

**Layers**:
1. **Presentation** - Controllers, REST endpoints
2. **Application** - Services, DTOs, Business logic
3. **Domain** - Entities, Repositories
4. **Infrastructure** - Config, External clients

**Dependency Rule**: `Presentation → Application → Domain ← Infrastructure`

**Enforcement**: Structural tests in each service validate layer boundaries

**Rationale**: Clear separation of concerns makes code predictable for both humans and agents.

---

## Event-Driven Architecture

**Principle**: Prefer async events over sync calls for cross-service communication

**When to use Kafka**:
- State changes (order created, payment succeeded)
- Fire-and-forget operations (send notification)
- Eventual consistency acceptable

**When to use Feign**:
- Immediate response needed (query product details)
- User-facing operations (checkout flow)

**Rationale**: Events decouple services, improve resilience, and enable independent scaling.

---

## Fail Fast, Fail Explicitly

**Principle**: Surface errors early and clearly

**In Practice**:
- Input validation at boundaries (controller layer)
- Custom exception types with clear messages
- No silent failures or swallowed exceptions
- Structured error responses

**Example**:
```java
// ❌ Bad: Silent failure
if (product == null) {
    return null;
}

// ✅ Good: Fail explicitly
if (product == null) {
    throw new ProductNotFoundException(productId);
}
```

**Rationale**: Agents and humans both need clear error signals to fix issues quickly.

---

## Boring Technology

**Principle**: Prefer proven, stable technologies over cutting-edge

**Technology Choices**:
- Java 17 (LTS, not bleeding-edge 21+)
- Spring Boot 3.4.4 (stable, well-documented)
- PostgreSQL (not NoSQL for core data)
- Kafka (not newer event systems)

**Rationale**: Stable tech = better documentation = easier for agents to work with = fewer surprises.

---

## Test Pyramid

**Principle**: More unit tests, fewer integration tests, minimal E2E

**Ratios**:
- **70%** Unit tests (fast, isolated)
- **25%** Integration tests (DB, Kafka)
- **5%** Manual/exploratory testing

**Enforcement**: Build fails below 70% coverage

**Rationale**: Fast feedback loop for agents and developers.

---

## Idempotent Operations

**Principle**: Same request multiple times = same result

**Critical for**:
- Kafka consumers (duplicate messages possible)
- Payment operations (no double-charging)
- Inventory operations (no duplicate decreases)

**Pattern**:
```java
@KafkaListener(topics = "order.created")
public void handleOrder(OrderEvent event) {
    if (alreadyProcessed(event.getId())) {
        return; // Idempotent
    }
    processOrder(event);
    markProcessed(event.getId());
}
```

**Rationale**: Distributed systems have retries; operations must be safe to repeat.

---

## Observability by Default

**Principle**: Every service emits logs, metrics, and traces

**Three Pillars**:
1. **Logs**: Structured (JSON), contextual (traceId)
2. **Metrics**: RED (Rate, Errors, Duration)
3. **Traces**: Distributed tracing via Zipkin

**What to instrument**:
- All HTTP endpoints
- All Kafka producers/consumers
- All database queries (via JPA metrics)
- All external API calls (Feign)

**Rationale**: Agents can verify behavior by inspecting observability data (logs, metrics, traces).

---

## API-First Design

**Principle**: Design API contract before implementation

**Process**:
1. Write OpenAPI spec (Swagger)
2. Review API design
3. Implement controller
4. Auto-validate against spec (SpringDoc)

**Benefits**:
- Frontend and backend can work in parallel
- Contract testing easier
- API documentation always up-to-date

**Rationale**: Clear contracts make integration predictable.

---

## Security by Design

**Principle**: Security is not an afterthought

**Mandatory**:
- Input validation (`@Valid` on all request DTOs)
- Authentication (JWT on all protected endpoints)
- Authorization (role-based access control)
- Secrets management (never hard-code, use env vars)

**Forbidden**:
- SQL injection (use JPA/QueryDSL, not raw SQL)
- XSS (sanitize user input)
- CSRF (for state-changing operations)

**Rationale**: Preventing vulnerabilities from the start is cheaper than fixing later.

---

## Incremental Changes

**Principle**: Small, frequent changes over big rewrites

**PR Guidelines**:
- One logical change per PR
- Max 500 lines changed (exceptions allowed)
- Fast review cycle (< 1 day)

**Deployment**:
- Independent service deployments
- Feature flags for gradual rollouts
- Blue-green deployment (future)

**Rationale**: Smaller changes = easier to review, test, and rollback.

---

## Documentation is Code

**Principle**: Documentation lives in repository, versioned with code

**What to document**:
- API contracts (Swagger)
- Architecture decisions (design-docs)
- Event schemas (KAFKA_EVENTS.md)
- Quality metrics (QUALITY_SCORE.md)

**What NOT to document**:
- Implementation details (code should be self-explanatory)
- Obvious patterns (don't repeat what's in Spring docs)

**Update triggers**:
- Architecture change → Update ARCHITECTURE.md
- New pattern → Add to design-docs/
- Quality issue → Update QUALITY_SCORE.md

**Rationale**: Documentation drift is prevented when docs are treated as code.

---

## Mechanical Enforcement

**Principle**: Important rules are enforced by tools, not just code review

**Enforcement Mechanisms**:
- **Linters**: Custom linting rules for architecture
- **Tests**: Structural tests for layer violations
- **CI**: Build fails on coverage drops, security vulns
- **Pre-commit hooks**: Format, lint before commit (future)

**Example**:
```java
// Structural test enforcing layering
@Test
void presentation_layer_should_not_depend_on_infrastructure() {
    JavaClasses classes = new ClassFileImporter()
        .importPackages("com.live_commerce.order");

    ArchRule rule = noClasses()
        .that().resideInAPackage("..presentation..")
        .should().dependOnClassesThat()
        .resideInAPackage("..infrastructure..");

    rule.check(classes);
}
```

**Rationale**: Tools never forget; humans do.

---

## Learn from Failures

**Principle**: Every incident is a learning opportunity

**Post-Incident Process**:
1. Document issue in `docs/quality/QUALITY_SCORE.md`
2. Add test to prevent recurrence
3. Update docs if knowledge gap found
4. Consider if enforcement rule needed

**Blameless Culture**: Focus on system improvement, not individual fault.

**Rationale**: Systematic improvement over time.

---

## References

- **[ARCHITECTURE.md](../ARCHITECTURE.md)** - How these principles are applied
- **[MSA_PATTERNS.md](MSA_PATTERNS.md)** - Concrete pattern implementations
- **[design-docs/index.md](design-docs/index.md)** - Specific design decisions

---

**Last Updated**: 2026-04-23
**Maintained By**: Engineering Team
