## Recurring weekly schedule

The clinic projection includes `weeklyClosures`, keyed by ISO weekday with `MORNING`, `EVENING` or `BOTH`. Missing or empty data means both sessions are open every weekday.

The server evaluates the requested appointment date in the clinic timezone. A matching closure rejects that session before capacity or token allocation. A partial closure does not affect the other session. Date-range Availability blocks are evaluated separately and may add exceptional closures.
# DO-LO Shared Backend Contract - Stage 10

## Scope

This contract aligns the Doctor and Patient Android apps without selecting a hosting provider. The Stage 10 APK uses LocalMockSharedBackendGateway; it never sends network traffic and cannot synchronize two devices. A production HTTPS implementation must preserve the same revision and idempotency rules.

## Authority

The future backend is authoritative for:

- authenticated Doctor, Assistant, Patient and Admin identities;
- server-enforced role and Assistant permissions;
- clinic-local dates, session schedules and availability;
- atomic token allocation per clinic, date and session;
- appointment, payment, receipt and queue transitions;
- announcements, delay notices, feedback and immutable audit records;
- cross-device delivery and conflict resolution.

Android clients must not allocate production tokens or settle conflicts independently.

## Clinic snapshot

A snapshot is scoped to one clinicId and one queue date. It contains:

- monotonically increasing revision;
- server-generated timestamp;
- clinic and schedule configuration;
- Morning and Evening queue projections;
- appointments with permanent token and mutable queue order;
- live/draft announcements and availability controls;
- active queue-delay notices.

Sensitive credentials, password/PIN hashes and private clinical notes are excluded.

## Commands

### Publish Doctor snapshot

PUT /v1/clinics/{clinicId}/operational-snapshot

Required headers:

- Authorization: Bearer access-token
- Idempotency-Key: uuid
- If-Match: revision

The server returns the accepted snapshot and new revision. A stale If-Match returns HTTP 409 with the current server revision. Replaying the same idempotency key returns the original successful result without duplicating audit events.

### Create Patient appointment

POST /v1/clinics/{clinicId}/appointments

The request carries appointment date, Morning/Evening session, patient identity reference, family-member display name and an idempotency key. The server validates session availability and token capacity inside one transaction, then allocates the next token for that session.

A Patient App booking starts as BOOKED and fee PENDING. It does not receive a queue order and cannot enter consultation until authorized clinic staff confirms payment or waiver and generates the compulsory receipt.

### Pull clinic state

GET /v1/clinics/{clinicId}/operational-snapshot?date=yyyy-MM-dd

The response uses an ETag/revision. Clients may request changes after a known revision later, but Stage 10 uses a complete clinic-day snapshot to keep reconciliation explicit.

## Conflict rules

1. The server rejects stale mutations; it never silently overwrites a newer queue.
2. The Doctor App must pull and display the current revision before retrying.
3. Appointment status transitions are server validated.
4. Token numbers never change after allocation. The server transactionally inserts upcoming tokens in order, anchors late arrivals to the active consultation, orders each same-anchor cohort by token number, applies the late buffer, and changes queueOrder only through admission or authorized skip/rejoin operations.
5. Morning and Evening token sequences are independent.
6. Closing one session cannot close or disable the other.
7. Duplicate idempotency keys return the original result.
8. Offline local changes remain PENDING until accepted by the server.

## Financial boundary

- consultationFee and paymentStatus in an appointment are clinic-reported operational evidence only; DO-LO does not create a gateway order or settle the doctor's consultation fee.
- Patient booking service charges are paid to DO-LO online and use a separate Admin-owned ledger and idempotent gateway transaction.
- Doctor platform charges use a separate weekly/monthly Admin-owned billing ledger derived from the approved appointment/count policy.
- Consultation receipts, patient service-charge receipts and doctor platform invoices are different documents and must have independent identifiers, audit trails, reconciliation and refund/dispute rules.


## Security and privacy gate

Before replacing the mock transport, add HTTPS certificate validation, short-lived access tokens with refresh rotation, device/session revocation, server rate limits, structured audit retention, encrypted secrets, minimum necessary patient fields, consent/privacy documentation and environment-separated backend URLs. SMS, maps, payment and push credentials remain server-side only.
## Future-booking policy

The clinic projection includes futureBookingEnabled and advanceBookingDays. The server evaluates requested dates using the clinic timezone:

- Patient App current-day requests remain allowed when the session is open.
- Future Patient App requests require futureBookingEnabled and cannot exceed advanceBookingDays.
- Clinic walk-in requests require the server clinic date regardless of the Patient App policy.
- Date policy passes before availability, session capacity, payment and token allocation checks.
- Policy changes increment the clinic revision and create an immutable audit event.

The Stage 11 local transport evaluates this rule but stores only the active clinic-day snapshot. The hosted backend must own scheduled future appointments.

## Stage 12 Android transport modes

The Doctor App now declares two explicit client modes:

- LOCAL_MOCK is the default and retains deterministic in-process contract testing.
- REMOTE_DISABLED is a fail-closed readiness stub. It validates an HTTPS endpoint description but makes no request and returns a non-retryable disabled result for every operation.

FutureProviderFlags default to false and configuration validation rejects SMS, DO-LO service-charge payment, maps or push activation. The Stage 12 manifest intentionally omits Android Internet permission. A later network-enabled release requires a reviewed hosted API, environment-specific HTTPS endpoint, authenticated transport implementation, secret-free client configuration and approval of the security/privacy gate above.
