# OneTick Enterprise Platform Roadmap

## Vision
Build OneTick into a production-grade work management platform comparable to Asana, Jira, monday.com, and ClickUp, with stronger enterprise governance, AI safety, and operational reliability.

## Target Reference Architecture
- Experience layer: web app, mobile clients, OpenAPI-based integrations.
- Core domain services: identity, workspaces, projects, tasks, goals, automation, notifications.
- Data layer: PostgreSQL for transactional workloads, Redis for cache/queues, object store for attachments.
- Integration layer: outbound connectors (email/chat/calendar/BI), inbound webhooks, event bus.
- Platform layer: audit, observability, policy engine, tenancy controls, backup/recovery.

## Delivery Phases

### Phase 1: Production Foundation (completed)
- Environment-driven configuration and runtime profiles.
- Containerized deployment (`Dockerfile`, `docker-compose`).
- CI pipeline and repeatable build.
- Health/readiness endpoints and secure defaults.
- Browser-based validation UX and OpenAPI docs.

### Phase 2: Multi-Tenant Workspace Model (baseline complete)
- Workspace/project domain model with tenant-scoped APIs.
- Tenant-aware authorization in core services.
- Pagination/filtering on list endpoints.
- Remaining: organization boundaries, memberships, full data-isolation strategy.

### Phase 3: Enterprise Governance (baseline complete)
- Audit logs with export API and admin-only access.
- Proper access-denied handling for tenant-scoped APIs.
- OIDC scaffolding with Auth0 login/callback.
- Remaining: SSO (OIDC/SAML), SCIM, data retention/soft-delete/legal hold, secrets management/key rotation.

### Phase 4: Automation and AI (baseline complete)
- Workflow automation rules (triggers/actions).
- AI action proposals with human-in-the-loop approvals.
- Remaining: AI summarization/planning/risk detection, prompt/version governance, action traceability.

### Phase 5: Scale and Reliability (in progress)
- Async processing with message queue (Redis-backed notification queue delivered with retries + DLQ).
- Caching strategy, read optimization, and pagination/search index (Redis cache enabled for hot entity reads).
- SLOs, canary rollout, blue/green deployment model.
- Backup/restore drills and disaster recovery readiness.

## Non-Functional Requirements
- Availability: 99.9%+ target for API and UI.
- Security: JWT + rotating signing key strategy; zero hardcoded secrets.
- Observability: metrics, traces, logs with correlation ID.
- Performance: p95 API latency targets by endpoint class.
- Compliance: change management and auditable admin actions.

## Immediate Next Sprint Backlog
1. Expand integration tests for role checks, automation rules, and audit exports.
2. Introduce release versioning and API deprecation policy.
3. Add cache hit/miss metrics and dashboard baseline.
4. Start SSO discovery: pick OIDC provider and define auth flows.
5. Define SLOs and alert thresholds for core APIs.
