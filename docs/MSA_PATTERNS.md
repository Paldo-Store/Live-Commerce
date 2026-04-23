# MSA Patterns Guide

**Common microservices patterns used in Live Commerce Platform**

---

## Table of Contents

1. [API Gateway Pattern](#api-gateway-pattern)
2. [Service Discovery Pattern](#service-discovery-pattern)
3. [Saga Pattern (Event-Driven)](#saga-pattern-event-driven)
4. [Circuit Breaker Pattern](#circuit-breaker-pattern)
5. [Database Per Service](#database-per-service)
6. [Event Sourcing & CQRS](#event-sourcing--cqrs)
7. [Distributed Lock Pattern](#distributed-lock-pattern)
8. [Cache-Aside Pattern](#cache-aside-pattern)

---

## API Gateway Pattern

**Purpose**: Single entry point for all client requests

**Implementation**: Spring Cloud Gateway

**Benefits**:
- Centralized authentication/authorization
- Request routing based on path
- Load balancing across service instances
- Rate limiting and throttling
- Request/response transformation

**Example**:
```yaml
# gateway/application.yml
spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://USER-SERVICE
          predicates:
            - Path=/api/users/**
          filters:
            - JwtAuthenticationFilter
```

**When to use**: Always (for external-facing APIs)

**Trade-offs**:
- ➕ Simplified client logic
- ➕ Centralized security
- ➖ Single point of failure (mitigate with HA)
- ➖ Potential bottleneck (use caching, async)

---

## Service Discovery Pattern

**Purpose**: Dynamic service location without hard-coded URLs

**Implementation**: Netflix Eureka

**How it works**:
1. Services register themselves with Eureka on startup
2. Services send heartbeats to Eureka (default: 30s)
3. Clients query Eureka to find service instances
4. Client-side load balancing via Ribbon/LoadBalancer

**Example**:
```java
// Service registration (automatic with @EnableEurekaClient)
@SpringBootApplication
@EnableEurekaClient
public class OrderApplication {
    // ...
}

// Service discovery via Feign
@FeignClient(name = "product-service") // Uses Eureka to resolve
public interface ProductClient {
    @GetMapping("/api/products/{id}")
    ProductResponse getProduct(@PathVariable Long id);
}
```

**When to use**: Always (in microservices)

**Trade-offs**:
- ➕ No hard-coded service URLs
- ➕ Automatic failover
- ➖ Additional infrastructure component
- ➖ Network overhead for health checks

---

## Saga Pattern (Event-Driven)

**Purpose**: Manage distributed transactions across services

**Type**: Choreography-based (no orchestrator)

**How it works**:
1. Service A starts transaction, publishes event
2. Service B listens, performs action, publishes event
3. Continue until all steps complete
4. If any step fails, publish compensating events

**Example: Order Creation Saga**

```
Order Service (create order)
    ↓ publish "order.created"
Product Service (decrease inventory)
    ↓ publish "inventory.decreased"
Coupon Service (mark coupon used)
    ↓ publish "coupon.used"
Payment Service (charge customer)
    ↓ publish "payment.success" OR "payment.failed"
Order Service (mark order complete/failed)

// Compensation flow (if payment fails)
Payment Service
    ↓ publish "payment.failed"
Product Service (restore inventory)
    ↓ publish "inventory.restored"
Coupon Service (restore coupon)
    ↓ publish "coupon.restored"
Order Service (mark order cancelled)
```

**Implementation**:
```java
// Order Service
@Transactional
public void createOrder(OrderRequest request) {
    Order order = orderRepository.save(new Order(request));
    kafkaTemplate.send("order.created", new OrderCreatedEvent(order));
}

// Product Service
@KafkaListener(topics = "order.created")
@Transactional
public void decreaseInventory(OrderCreatedEvent event) {
    try {
        inventoryService.decrease(event.getProductId(), event.getQuantity());
        kafkaTemplate.send("inventory.decreased", new InventoryDecreasedEvent(...));
    } catch (InsufficientStockException e) {
        kafkaTemplate.send("inventory.failed", new InventoryFailedEvent(...));
    }
}

// Compensation in Product Service
@KafkaListener(topics = "payment.failed")
@Transactional
public void restoreInventory(PaymentFailedEvent event) {
    inventoryService.increase(event.getProductId(), event.getQuantity());
    kafkaTemplate.send("inventory.restored", new InventoryRestoredEvent(...));
}
```

**When to use**:
- Multi-service transactions
- Eventual consistency acceptable
- Need for high availability

**Trade-offs**:
- ➕ No single point of failure
- ➕ Loose coupling
- ➖ Complex to debug
- ➖ Eventual consistency (not immediate)

---

## Circuit Breaker Pattern

**Purpose**: Prevent cascade failures when downstream service fails

**Implementation**: Resilience4j (recommended, not yet implemented)

**States**:
1. **Closed**: Normal operation, requests pass through
2. **Open**: Threshold exceeded, requests fail fast
3. **Half-Open**: Testing if service recovered

**Example** (to be implemented):
```java
@CircuitBreaker(name = "productService", fallbackMethod = "getProductFallback")
public ProductResponse getProduct(Long productId) {
    return productClient.getProduct(productId);
}

private ProductResponse getProductFallback(Long productId, Exception e) {
    log.error("Product service unavailable, using fallback", e);
    return ProductResponse.builder()
        .id(productId)
        .name("Product temporarily unavailable")
        .available(false)
        .build();
}
```

**Configuration**:
```yaml
resilience4j:
  circuitbreaker:
    instances:
      productService:
        failure-rate-threshold: 50
        wait-duration-in-open-state: 10s
        permitted-number-of-calls-in-half-open-state: 3
```

**When to use**:
- Synchronous service calls (Feign)
- External API integrations
- High availability requirements

**Trade-offs**:
- ➕ Prevents cascade failures
- ➕ Fast failure response
- ➖ Requires fallback logic
- ➖ May hide underlying issues

---

## Database Per Service

**Purpose**: Each service owns its data, enforcing boundaries

**Current State**: Shared PostgreSQL (schema separation)

**Target State**: Separate databases per service

**Migration Strategy**:

**Phase 1** (Current):
```
PostgreSQL (livecommerce)
├── user_schema
├── product_schema
├── order_schema
└── ...
```

**Phase 2** (Future):
```
PostgreSQL (user_db)
PostgreSQL (product_db)
PostgreSQL (order_db)
...
```

**Rules**:
1. ❌ No direct cross-service DB queries
2. ✅ Use Feign for sync data access
3. ✅ Use Kafka events for data replication

**Example** (Data Replication):
```java
// User Service publishes user updates
@KafkaListener(topics = "user.updated")
public void handleUserUpdated(UserUpdatedEvent event) {
    // Order service maintains local user cache
    userCacheRepository.save(new UserCache(event));
}
```

**When to use**: Always (for true service independence)

**Trade-offs**:
- ➕ True service independence
- ➕ Scalability per service
- ➖ Data duplication
- ➖ Complex joins (need aggregation services)

---

## Event Sourcing & CQRS

**Purpose**: Separate read and write models, store events

**Current State**: Not fully implemented (future enhancement)

**Concept**:
- **Command Model**: Handles writes, stores events
- **Query Model**: Optimized read-only views

**Example Use Case**: Order Service

```java
// Command side (write)
public class OrderAggregate {
    private List<DomainEvent> events = new ArrayList<>();

    public void createOrder(OrderRequest request) {
        // Validate
        OrderCreatedEvent event = new OrderCreatedEvent(...);
        events.add(event);
        apply(event);
    }

    private void apply(OrderCreatedEvent event) {
        this.id = event.getOrderId();
        this.status = OrderStatus.PENDING;
    }
}

// Query side (read)
@Entity
public class OrderView {
    // Denormalized view optimized for queries
    private Long orderId;
    private String userName;
    private List<String> productNames;
    private BigDecimal totalAmount;
}

// Projection
@KafkaListener(topics = "order.created")
public void projectOrderView(OrderCreatedEvent event) {
    OrderView view = buildOrderView(event);
    orderViewRepository.save(view);
}
```

**When to use**:
- Audit trail requirements
- Complex domain logic
- High read/write ratio

**Trade-offs**:
- ➕ Full audit history
- ➕ Optimized read models
- ➖ Increased complexity
- ➖ Eventual consistency

---

## Distributed Lock Pattern

**Purpose**: Ensure only one process modifies shared resource

**Implementation**: Redisson (Redis-based)

**Use Case**: Inventory decrease in Product Service

**Example**:
```java
@Service
public class InventoryService {

    private final RedissonClient redissonClient;

    @Transactional
    public void decreaseInventory(Long productId, Integer quantity) {
        String lockKey = "inventory:lock:" + productId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // Wait up to 5s to acquire, auto-release after 10s
            boolean acquired = lock.tryLock(5, 10, TimeUnit.SECONDS);

            if (!acquired) {
                throw new LockAcquisitionException("Could not acquire lock");
            }

            // Critical section
            Inventory inventory = inventoryRepository.findByProductId(productId);
            if (inventory.getStock() < quantity) {
                throw new InsufficientStockException();
            }

            inventory.decrease(quantity);
            inventoryRepository.save(inventory);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LockException("Lock interrupted", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
```

**When to use**:
- High concurrency operations
- Shared resource modification
- Inventory, coupon issuance, etc.

**Trade-offs**:
- ➕ Prevents race conditions
- ➕ Distributed (works across instances)
- ➖ Performance overhead
- ➖ Redis dependency (single point of failure)

---

## Cache-Aside Pattern

**Purpose**: Improve read performance with caching

**Implementation**: Redis with Spring Cache

**Use Case**: Product details caching

**Example**:
```java
@Service
public class ProductService {

    @Cacheable(value = "products", key = "#productId")
    public ProductResponse getProduct(Long productId) {
        // Cache miss: Query DB
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException(productId));
        return ProductMapper.toResponse(product);
    }

    @CacheEvict(value = "products", key = "#productId")
    public void updateProduct(Long productId, ProductUpdateRequest request) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException(productId));

        product.update(request);
        productRepository.save(product);
        // Cache automatically evicted
    }

    @Caching(evict = {
        @CacheEvict(value = "products", key = "#productId"),
        @CacheEvict(value = "productRankings", allEntries = true)
    })
    public void deleteProduct(Long productId) {
        productRepository.deleteById(productId);
    }
}
```

**Cache Configuration**:
```java
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(10))
            .disableCachingNullValues()
            .serializeValuesWith(/* Jackson serializer */);
    }
}
```

**When to use**:
- Read-heavy operations
- Expensive computations
- Rarely changing data

**Trade-offs**:
- ➕ Significant performance boost
- ➕ Reduced DB load
- ➖ Cache invalidation complexity
- ➖ Stale data risk

---

## Anti-Patterns to Avoid

### ❌ Shared Database

**Problem**: Multiple services accessing same database tables

**Why bad**: Tight coupling, can't scale independently

**Solution**: Use API calls or events for cross-service data

---

### ❌ Chatty Services

**Problem**: Service A calls B, B calls C, C calls D for single request

**Why bad**: High latency, cascade failures

**Solution**: Aggregate data, use async events, cache

---

### ❌ Distributed Monolith

**Problem**: Microservices but tightly coupled (must deploy together)

**Why bad**: No benefits of MSA, only complexity

**Solution**: Enforce loose coupling via events, versioned APIs

---

### ❌ God Service

**Problem**: One service handles too many responsibilities

**Why bad**: Hard to scale, violates SRP

**Solution**: Split into smaller domain services

---

## Pattern Selection Guide

| Requirement | Pattern | Priority |
|-------------|---------|----------|
| External API access | API Gateway | Critical |
| Service location | Service Discovery | Critical |
| Distributed transactions | Saga | High |
| Prevent cascade failures | Circuit Breaker | High |
| Data ownership | Database Per Service | High |
| High concurrency | Distributed Lock | Medium |
| Read performance | Cache-Aside | Medium |
| Audit trail | Event Sourcing | Low (future) |

---

**Last Updated**: 2026-04-23
**Maintained By**: Engineering Team
