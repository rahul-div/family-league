# Decision Log

| # | Decision | Justification |
|---|---|---|
| DL-001 | PostgreSQL over MySQL | Better UUID support (`gen_random_uuid()`), CHECK constraints, trigger functions, TIMESTAMPTZ for timezone-aware timestamps |
| DL-002 | Feature-based package structure (vertical slices) | Each module (auth, league, match, etc.) is self-contained with its own entity, controller, service, repository, dto, exception sub-packages. Easier to navigate and maintain than horizontal layering |
| DL-003 | Soft delete for all entities | Requirement: no permanent deletes. `BaseEntity` provides `softDelete()` method, `@SQLRestriction("is_deleted = false")` auto-filters. Only `app_config` has no soft-delete (simple key-value, no audit needed) |
| DL-004 | DB triggers for prediction lock | Requirement: "enforced at database level" — defense in depth. Application-layer checks can be bypassed; triggers provide a safety net at the PostgreSQL level |
| DL-005 | JWT without revocation | Acceptable for family-scale app; 24h token expiry provides sufficient security. Adding a blacklist/revocation list would add complexity with minimal benefit for this use case |
| DL-006 | Caffeine over Redis | Single-instance application; no need for distributed cache. Caffeine is in-process, zero-config, and sufficient for our caching needs (config, leagues, teams, players) |
| DL-007 | Testcontainers for integration/E2E tests | Real PostgreSQL in tests catches DB-specific issues (triggers, CHECK constraints, UUID generation) that H2 or mocks would miss |
| DL-008 | 3 separate async thread pools | Isolate scoring (`scoreExecutor`), email (`emailExecutor`), and general (`taskExecutor`) workloads to prevent resource contention. Scoring shouldn't block email delivery and vice versa |
| DL-009 | Season result hard-delete on re-publish | Admin may need to correct final standings; old season score details become invalid. This is the only justified hard-delete in the system |
| DL-010 | Simulated email mode | When `spring.mail.username` is blank, log emails to console instead of SMTP send. Enables development without external SMTP dependency |
| DL-011 | Java records for DTOs | Immutable, concise, no Lombok needed. Entities use Lombok (`@Builder`, `@Getter`, `@Setter`) for mutability needs |
| DL-012 | Interface-driven services | All 14 services have interface + implementation. Enables mocking in tests, supports future AOP proxying, and matches the requirements document |
| DL-013 | TransactionSynchronization.afterCommit for scoring | Score calculation and leaderboard recalculation happen async AFTER the result transaction commits. Prevents partial state if the transaction rolls back |
| DL-014 | Email retry with max attempts (3) | Emails in PENDING status are retried up to 3 times. After that, status becomes FAILED. Prevents infinite retry loops on permanently undeliverable emails |
