# School microservices

This folder contains bounded contexts extracted from the legacy school backend.

## Architecture rule

Each service follows hexagonal architecture:

- `domain`: pure business model and invariants.
- `application/port/in`: use case contracts.
- `application/port/out`: persistence or integration ports.
- `application/service`: orchestration of use cases.
- `adapter/in/rest`: REST API adapter.
- `adapter/out/persistence`: JPA adapter.
- `resources/db/migration`: Flyway migrations owned by the service.

## Services

- `billing-service`: payments, receipts, payment summary.
- `attendance-service`: daily attendance, absence justification, attendance summary.

## Local ports

- Billing: `http://localhost:8082/api`
- Attendance: `http://localhost:8083/api`

The Angular frontend has dedicated environment keys:

- `billingApiUrl`
- `attendanceApiUrl`

This keeps the legacy backend available on `apiUrl` while new bounded contexts move to independent runtimes.
