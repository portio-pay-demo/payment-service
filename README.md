# payment-service

Core payment processing service for PortIOPay. Handles payment authorization, settlement, rate limiting, and PCI-DSS audit logging.

## Overview

The payment-service is the Tier 1 critical path for all PortIOPay transactions. It processes ~2M transactions/day with a P99 SLA of <500ms.

**Tech Stack:** Java 17, Spring Boot 3, PostgreSQL, Redis, Kafka

## Key Features

- Payment authorization and capture
- Idempotent transaction processing
- Token bucket rate limiting per merchant
- Exponential backoff with circuit breaker on gateway timeouts
- PCI DSS 4.0 compliant audit logging (shipped to Splunk)
- Reconciliation job (nightly, configurable window)

## Local Development

```bash
./mvnw spring-boot:run
```

Requires: Java 17+, Docker (for local Postgres + Redis via docker-compose)

```bash
docker compose up -d
./mvnw spring-boot:run -Dspring.profiles.active=local
```

## Deployment

Deployed to Kubernetes (EKS). Helm chart in `infra/helm/payment-service`.

- **Production**: `payment-service.portioapay.internal:8080`
- **Staging**: `payment-service.staging.portioapay.internal:8080`

## Ownership

- Team: **PortIOPay Payments**
- CODEOWNERS: `@payments-team`, `@checkout-leads`, `@finance-eng`
- On-call: PagerDuty service `portioapay-payments-prod`

## API

```
POST /api/v1/payments/authorize
POST /api/v1/payments/capture
POST /api/v1/payments/refund
GET  /api/v1/payments/{transactionId}
GET  /actuator/health
```
