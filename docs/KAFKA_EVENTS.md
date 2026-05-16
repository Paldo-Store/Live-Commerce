# Kafka Events Catalog

**Complete catalog of Kafka events in the Live Commerce Platform**

---

## Event Naming Convention

Pattern: `<domain>.<action>` (e.g., `order.created`, `payment.success`)

**Actions**:
- `created` - New entity created
- `updated` - Entity modified
- `deleted` - Entity removed
- `success` - Operation succeeded
- `failed` - Operation failed

---

## Event Flows

### 1. User Registration Flow

```
User Service → user.signup
                   ↓
             Coupon Service (consume)
                   ↓
             Issue welcome coupon
```

**Event**: `user.signup`
**Producer**: User Service
**Consumers**: Coupon Service
**Payload**:
```json
{
  "userId": "Long",
  "email": "String",
  "signupTimestamp": "Instant"
}
```

---

### 2. Order Creation Flow

```
Order Service → order.created
                   ↓
        ┌──────────┴──────────┐
        ↓                     ↓
  Product Service      Coupon Service
  (decrease stock)     (mark used)
        ↓
  inventory.decreased
        ↓
  Order Service (consume)
```

**Event**: `order.created`
**Producer**: Order Service
**Consumers**: Product Service, Coupon Service
**Payload**:
```json
{
  "orderId": "Long",
  "userId": "Long",
  "products": [
    {
      "productId": "Long",
      "quantity": "Integer",
      "price": "BigDecimal"
    }
  ],
  "couponId": "Long | null",
  "totalAmount": "BigDecimal",
  "createdAt": "Instant"
}
```

---

**Event**: `inventory.decreased`
**Producer**: Product Service
**Consumers**: Order Service
**Payload**:
```json
{
  "orderId": "Long",
  "productId": "Long",
  "decreasedQuantity": "Integer",
  "remainingStock": "Integer",
  "timestamp": "Instant"
}
```

---

### 3. Payment Flow

```
Payment Service
    ↓
  (KakaoPay API call)
    ↓
payment.success OR payment.failed
    ↓
Order Service (update status)
```

**Event**: `payment.success`
**Producer**: Payment Service
**Consumers**: Order Service
**Payload**:
```json
{
  "paymentId": "Long",
  "orderId": "Long",
  "amount": "BigDecimal",
  "paymentMethod": "String",
  "transactionId": "String",
  "paidAt": "Instant"
}
```

**Event**: `payment.failed`
**Producer**: Payment Service
**Consumers**: Order Service, Product Service (restore inventory)
**Payload**:
```json
{
  "paymentId": "Long",
  "orderId": "Long",
  "failureReason": "String",
  "errorCode": "String",
  "failedAt": "Instant"
}
```

---

### 4. Inventory Management Flow

```
Product Service → inventory.increased
                      ↓
              (Event log for auditing)

Product Service → inventory.decreased
                      ↓
              Order Service (confirm)
```

**Event**: `inventory.increased`
**Producer**: Product Service
**Consumers**: (Optional) Analytics Service
**Payload**:
```json
{
  "productId": "Long",
  "increasedQuantity": "Integer",
  "newStock": "Integer",
  "reason": "String",  // "RESTOCK", "ORDER_CANCELLED", etc.
  "timestamp": "Instant"
}
```

---

### 5. Broadcast Notification Flow

```
Broadcast Service → broadcast.scheduled
                         ↓
                 Notification Service
                         ↓
            (Send notifications 10 min before)
```

**Event**: `broadcast.scheduled`
**Producer**: LiveBroadcast Service
**Consumers**: Notification Service
**Payload**:
```json
{
  "broadcastId": "Long",
  "title": "String",
  "scheduledStartTime": "Instant",
  "subscriberIds": ["Long"],
  "createdAt": "Instant"
}
```

**Event**: `broadcast.started`
**Producer**: LiveBroadcast Service
**Consumers**: Notification Service, Analytics
**Payload**:
```json
{
  "broadcastId": "Long",
  "title": "String",
  "startedAt": "Instant",
  "viewerCount": "Integer"
}
```

---

### 6. Coupon Flow

```
Coupon Service → coupon.issued
                      ↓
              User Service (notify)

Order Service → order.created
                      ↓
              Coupon Service
                      ↓
              coupon.used
```

**Event**: `coupon.issued`
**Producer**: Coupon Service
**Consumers**: User Service, Notification Service
**Payload**:
```json
{
  "couponId": "Long",
  "userId": "Long",
  "discountType": "PERCENTAGE | FIXED",
  "discountValue": "BigDecimal",
  "expiresAt": "Instant",
  "issuedAt": "Instant"
}
```

**Event**: `coupon.used`
**Producer**: Coupon Service
**Consumers**: Order Service (confirmation)
**Payload**:
```json
{
  "couponId": "Long",
  "orderId": "Long",
  "userId": "Long",
  "discountAmount": "BigDecimal",
  "usedAt": "Instant"
}
```

---

## Consumer Configuration

### Idempotency Pattern

All consumers **MUST** implement idempotency:

```java
@KafkaListener(topics = "order.created")
public void handleOrderCreated(OrderCreatedEvent event) {
    String eventId = event.getOrderId() + "-" + event.getCreatedAt();

    // Check if already processed
    if (eventRepository.existsByEventId(eventId)) {
        log.warn("Duplicate event detected: {}", eventId);
        return;
    }

    try {
        // Process event
        processOrder(event);

        // Mark as processed
        eventRepository.save(new ProcessedEvent(eventId));

    } catch (Exception e) {
        log.error("Failed to process event: {}", eventId, e);
        throw e; // Retry via Kafka
    }
}
```

### Consumer Groups

| Topic | Consumer Group | Service |
|-------|----------------|---------|
| `user.signup` | `coupon-consumer-group` | Coupon |
| `order.created` | `product-consumer-group` | Product |
| `order.created` | `coupon-consumer-group` | Coupon |
| `payment.success` | `order-consumer-group` | Order |
| `payment.failed` | `order-consumer-group` | Order |
| `inventory.decreased` | `order-consumer-group` | Order |
| `broadcast.scheduled` | `notification-consumer-group` | Notification |

---

## Error Handling

### Retry Strategy

**Kafka Configuration**:
```yaml
spring:
  kafka:
    consumer:
      enable-auto-commit: false
    listener:
      ack-mode: manual
```

**Retry Logic**:
1. First failure: Retry immediately (Kafka listener)
2. After 3 retries: Send to Dead Letter Queue (DLQ)
3. Manual intervention for DLQ messages

### Dead Letter Queue (DLQ)

**Pattern**: `<topic>.DLQ` (e.g., `order.created.DLQ`)

**Monitoring**: Alert on DLQ message count > 0

---

## Event Versioning

**Strategy**: Additive changes only

**Rules**:
1. ✅ Add new optional fields
2. ✅ Add new event types
3. ❌ Remove existing fields
4. ❌ Change field types
5. ❌ Rename fields

**Example** (Safe):
```json
// v1
{"userId": "Long", "email": "String"}

// v2 (backward compatible)
{"userId": "Long", "email": "String", "name": "String | null"}
```

---

## Testing Events

### Local Testing

Use Kafka UI: `http://localhost:8080`

**Steps**:
1. Navigate to Topics
2. Select topic (e.g., `order.created`)
3. Publish test message
4. Monitor consumer logs

### Integration Tests

```java
@SpringBootTest
@EmbeddedKafka(topics = {"order.created"})
class OrderKafkaIntegrationTest {

    @Autowired
    private KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    @Test
    void testOrderCreatedEvent() {
        // Given
        OrderCreatedEvent event = new OrderCreatedEvent(...);

        // When
        kafkaTemplate.send("order.created", event);

        // Then
        await().atMost(5, SECONDS)
               .until(() -> orderRepository.findById(event.getOrderId()).isPresent());
    }
}
```

---

## Monitoring

### Metrics

Track these via Prometheus:
- `kafka_consumer_lag` - Message backlog per consumer
- `kafka_consumer_records_consumed_total` - Total messages consumed
- `kafka_producer_record_send_total` - Total messages produced
- `kafka_consumer_failed_total` - Failed message processing

### Alerts

**Critical**:
- Consumer lag > 1000 messages
- DLQ message count > 0
- Consumer group rebalancing frequently

**Warning**:
- Consumer lag > 100 messages
- Processing time > 5 seconds

---

## Event Schema Registry (Future)

**Goal**: Enforce schema validation

**Tool**: Confluent Schema Registry or AWS Glue

**Benefit**:
- Prevent incompatible schema changes
- Auto-generate DTOs from schemas
- Version management

---

**Last Updated**: 2026-04-23
**Maintained By**: Engineering Team
