# Execution Plans

**Structured plans for complex work items**

---

## Purpose

Execution plans help break down complex features into manageable steps, track progress, and document decision-making.

---

## When to Create an Exec Plan

**Create a plan for**:
- Multi-service changes (e.g., new event flow)
- Architecture changes (e.g., database separation)
- Complex features (> 5 days of work)
- Performance optimization (requires measurement)

**Don't create a plan for**:
- Bug fixes (unless architectural)
- Simple CRUD endpoints
- Documentation updates
- Configuration changes

---

## Exec Plan Template

```markdown
# [Feature Name]

**Status**: 🔄 In Progress | ✅ Completed | ❌ Blocked

**Owner**: [Name]

**Started**: YYYY-MM-DD

**Target Completion**: YYYY-MM-DD

---

## Objective

[1-2 sentence summary of what we're building and why]

## Success Criteria

- [ ] Criterion 1 (measurable)
- [ ] Criterion 2 (measurable)
- [ ] Criterion 3 (measurable)

## Architecture

[High-level design, diagrams, key decisions]

## Tasks

### Phase 1: [Name]
- [ ] Task 1
- [ ] Task 2

### Phase 2: [Name]
- [ ] Task 3
- [ ] Task 4

## Testing Plan

- Unit tests: [What to test]
- Integration tests: [What to test]
- Manual testing: [Steps]

## Rollback Plan

[How to undo if things go wrong]

## Decision Log

### YYYY-MM-DD: [Decision]
**Context**: ...
**Options**: A, B, C
**Choice**: B
**Rationale**: ...

---

**Last Updated**: YYYY-MM-DD
```

---

## Active Plans

Plans in active development go in `active/`:

```
active/
├── circuit-breaker-implementation.md
├── database-separation.md
└── order-performance-optimization.md
```

---

## Completed Plans

Finished plans move to `completed/` for reference:

```
completed/
├── 2025-09-distributed-lock-implementation.md
├── 2025-10-kafka-event-catalog.md
└── 2026-01-websocket-chat-system.md
```

**Why keep them**: Historical context, patterns to reuse, lessons learned.

---

## Tech Debt Tracker

See `tech-debt-tracker.md` for ongoing technical debt items.

**Process**:
1. Identify tech debt (during dev or retro)
2. Add to tech-debt-tracker.md with priority
3. Create exec plan when ready to tackle
4. Move to completed/ when done

---

## References

- **[ARCHITECTURE.md](../../ARCHITECTURE.md)** - System constraints
- **[design-docs/](../design-docs/)** - Design patterns
- **[QUALITY_SCORE.md](../quality/QUALITY_SCORE.md)** - Quality tracking

---

**Last Updated**: 2026-04-23
