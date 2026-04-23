# Technical Debt Tracker

**Tracking known technical debt across the platform**

---

## High Priority (Fix within 1 sprint)

### 1. AI Service: No Integration Tests for Gemini API

**Impact**: High - Production failures difficult to debug
**Effort**: Medium (2-3 days)
**Owner**: TBD
**Created**: 2026-04-23

**Issue**: AI service calls Gemini API without integration tests. Failures only discovered in production.

**Proposal**:
- Add Gemini API mock for integration tests
- Test error handling (rate limits, timeouts, invalid responses)
- Test chat summarization end-to-end

**Acceptance Criteria**:
- [ ] Mock Gemini API client created
- [ ] 3+ integration tests covering success/failure cases
- [ ] Test coverage for AI service > 80%

---

### 2. Chat Service: WebSocket Connection Handling Not Tested

**Impact**: High - Cannot verify concurrent user handling
**Effort**: Medium (3-4 days)
**Owner**: TBD
**Created**: 2026-04-23

**Issue**: WebSocket connections tested manually only. No automated tests for concurrent connections, disconnections, or message broadcasting.

**Proposal**:
- Add WebSocket integration tests using Spring WebSocket test support
- Test concurrent connections (100+ users)
- Test message ordering and delivery guarantees

**Acceptance Criteria**:
- [ ] WebSocket test harness created
- [ ] Tests for connect/disconnect/broadcast scenarios
- [ ] Load test for 100+ concurrent connections

---

### 3. Order Service: Slow Query on Order History (>2s)

**Impact**: Medium - Poor user experience
**Effort**: Low (1 day)
**Owner**: TBD
**Created**: 2026-04-23

**Issue**: `GET /api/orders?userId={id}` takes >2 seconds for users with 100+ orders.

**Analysis**: Missing index on `orders.user_id`, full table scan.

**Proposal**:
- Add index: `CREATE INDEX idx_orders_user_id ON orders(user_id)`
- Add index: `CREATE INDEX idx_orders_created_at ON orders(created_at DESC)`
- Consider pagination (limit to 50 orders per request)

**Acceptance Criteria**:
- [ ] Indexes added via migration script
- [ ] Query time < 200ms (measured)
- [ ] Pagination implemented

---

## Medium Priority (Fix within 2 sprints)

### 4. Product Service: Cache Invalidation Logic Unclear

**Impact**: Medium - Potential stale data shown to users
**Effort**: Medium (2 days)
**Owner**: TBD
**Created**: 2026-04-23

**Issue**: Product cache eviction happens on update, but not always reliable. Unclear when cache vs DB is authoritative.

**Proposal**:
- Document cache strategy in code (TTL, eviction policy)
- Add cache metrics (hit rate, evictions)
- Consider write-through vs write-back pattern

---

### 5. Company Service: Missing API Documentation (40% of endpoints)

**Impact**: Medium - Developer experience issue
**Effort**: Low (1-2 days)
**Owner**: TBD
**Created**: 2026-04-23

**Issue**: Swagger docs missing for 8 out of 20 endpoints in Company service.

**Proposal**:
- Add `@Operation` annotations to all controllers
- Document request/response examples
- Add error response codes

---

### 6. Coupon Service: No Expiration Monitoring

**Impact**: Low - Manual cleanup needed periodically
**Effort**: Low (1 day)
**Owner**: TBD
**Created**: 2026-04-23

**Issue**: Expired coupons not automatically marked as invalid. Relies on manual cleanup scripts.

**Proposal**:
- Add scheduled job to mark expired coupons (daily at 00:00)
- Add Prometheus metric for expired coupon count
- Alert when expired coupons > 1000

---

## Low Priority (Fix within 3 sprints)

### 7. Gateway: Rate Limiting Not Implemented

**Impact**: Low - Not critical for internal beta
**Effort**: High (5-7 days)
**Owner**: TBD
**Created**: 2026-04-23

**Issue**: No rate limiting on Gateway. Vulnerable to abuse or accidental DoS.

**Proposal**:
- Implement rate limiting using Spring Cloud Gateway filters
- Use Redis for distributed rate limit tracking
- Different limits per endpoint (e.g., 100 req/min for auth, 1000 req/min for read)

---

### 8. All Services: Inconsistent Error Response Format

**Impact**: Low - Minor developer experience issue
**Effort**: Medium (3-4 days for standardization)
**Owner**: TBD
**Created**: 2026-04-23

**Issue**: Error responses differ across services. Some return:
```json
{"error": "Not found"}
```
Others return:
```json
{"message": "Not found", "code": "PRODUCT_NOT_FOUND"}
```

**Proposal**:
- Standardize on:
```json
{
  "timestamp": "2026-04-23T10:00:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Product with ID 123 not found",
  "path": "/api/products/123"
}
```
- Create common exception handler in shared library
- Roll out to all services incrementally

---

## Completed (Last 30 Days)

### ✅ Product Service: Added Redisson Distributed Lock

**Completed**: 2026-04-10
**Impact**: Prevented race conditions in inventory decrease

**Result**: No duplicate inventory deductions observed in 2 weeks.

---

### ✅ Payment Service: KakaoPay Integration Tests Added

**Completed**: 2026-04-15
**Impact**: Increased confidence in payment flow

**Result**: Test coverage for Payment service increased from 70% to 92%.

---

### ✅ User Service: JWT Validation Improved

**Completed**: 2026-04-18
**Impact**: Better security posture

**Result**: Expired tokens now properly rejected. Edge cases covered.

---

## Process

### Adding New Tech Debt

1. Identify issue (during development, code review, or incident)
2. Create entry in this file with:
   - Clear description
   - Impact assessment (High/Medium/Low)
   - Effort estimate (in days)
   - Proposed solution
3. Prioritize based on impact vs effort
4. Assign owner when ready to work on it

### Working on Tech Debt

1. Create exec plan in `active/` if complex (>2 days)
2. Mark as in-progress (add owner)
3. Implement solution
4. Update QUALITY_SCORE.md if applicable
5. Move to "Completed" section with date

### Reviewing Tech Debt

**Frequency**: Monthly (or after each sprint)

**Questions**:
- Are priorities still correct?
- Has new tech debt emerged?
- Can we batch similar items?

---

## Metrics

**Current Tech Debt Count**: 8 items

**By Priority**:
- High: 3 items
- Medium: 3 items
- Low: 2 items

**Oldest Item**: Chat Service WebSocket tests (identified 2026-04-23)

**Target**: Reduce high-priority items to 0 by end of Q2 2026

---

**Last Updated**: 2026-04-23
**Next Review**: 2026-05-23
