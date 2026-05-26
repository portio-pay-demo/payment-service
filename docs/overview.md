# PortIOPay Payment Service — Overview

Core payment processing microservice for the PortIOPay platform.

## Responsibilities

- Authorize, capture, and refund card payments via the external payment gateway
- Persist transaction state in PostgreSQL with idempotent request handling
- Enforce per-merchant rate limits using Redis token buckets
- Emit PCI DSS 4.0 audit events to Kafka for SIEM ingestion
- Run nightly reconciliation against gateway settlement files

## Runtime dependencies

| Dependency | Purpose |
|------------|---------|
| PostgreSQL | Transaction persistence |
| Redis | Rate limiting state |
| Kafka | Audit log streaming |
| Payment gateway | Card network authorization |

## Configuration

Application defaults live in `src/main/resources/application.yml`. Use Spring profiles for environment-specific settings:

- `local` — local development (see root `README.md`)
- Do not commit secrets; use environment variables or your secrets manager

## Related documentation

- [README](../README.md) — build, deploy, and API reference
- [CODEOWNERS](../CODEOWNERS) — review ownership and approval rules
