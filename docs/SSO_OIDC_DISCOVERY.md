# SSO OIDC Discovery

This document captures the initial OIDC discovery so we can start SSO implementation.

## Recommendation

Target IdP: **Auth0** (confirmed).

## OIDC Flow (Backend + Web UI)

1. **Authorization Code + PKCE** for browser clients.
2. Backend handles token exchange and session/JWT issuance.
3. Use the IdP `/.well-known/openid-configuration` for discovery.

## Required Claims

1. `sub` as stable user identifier.
2. `email` for login.
3. `name` or `given_name` + `family_name` for profile.
4. `groups` or a custom claim for role mapping if available.

## Role Mapping Baseline

1. `ADMIN` mapped from IdP group `onetick-admin`.
2. `MANAGER` mapped from IdP group `onetick-manager`.
3. `TEAM_LEAD` mapped from IdP group `onetick-team-lead`.
4. `STAFF` default for all authenticated users.

## Endpoints to Add (Planned)

1. `GET /api/v1/auth/oidc/login` redirect to Auth0 authorize endpoint.
2. `GET /api/v1/auth/oidc/callback` handle code exchange and issue JWT.
3. `POST /api/v1/auth/oidc/logout` revoke session if supported by Auth0.

## Implementation Status

- OIDC login and callback endpoints are implemented.

## Open Questions

1. Do we need SCIM provisioning in Phase 3, or will group sync be manual?
2. What is the preferred claim for group mapping (default `groups`)?
