# Architecture

## Current prototype

- Kotlin and Jetpack Compose single-module Android app
- Material 3 mobile-first UI
- `DoctorViewModel` for local prototype state
- immutable models and deterministic dummy data
- role-aware navigation for Doctor and Assistant
- provider-neutral service interfaces in `integrations/`
- GitHub Actions as the build environment

## Future boundaries

The Android app must never embed production SMS, payment, map, push or privileged backend credentials. A shared HTTPS backend will own authentication, authorization, doctor verification, appointment/token allocation, queue state, announcements, availability, broadcasts and audit logs.

## Planned backend resources

- `/auth/doctor/login`, `/auth/assistant/login`
- `/doctor/profile`, `/doctor/clinics`, `/doctor/schedules`
- `/doctor/queue`, `/doctor/appointments`
- `/doctor/availability-blocks`
- `/doctor/announcements`
- `/doctor/assistants`, `/doctor/assistants/{id}/permissions`
- `/admin/broadcasts`, `/admin/doctor-verification`

Every assistant action must be authorized on the server, not only hidden in Compose navigation.
## Stage 3 local queue lifecycle

- DoctorUiState.queueDate identifies the active clinic day using ISO yyyy-MM-dd.
- DailyQueueHistory is an immutable local snapshot of the closed date, final token and complete appointment list.
- QueueStateCodec uses URL-safe Base64 field encoding before storing current appointments and history records in SharedPreferences.
- Manual close is Doctor-only. A closed date rejects queue and appointment mutations.
- A later device date archives the previous queue once and initializes an empty NOT_STARTED queue.
- The shared backend will eventually replace device time and local storage as the authoritative clinic timezone, queue session and audit source.
## Stage 4 editable configuration

- Doctor profile and clinic mutations are accepted only while the active role is Doctor.
- Validation is enforced in DoctorViewModel in addition to Compose form constraints.
- Sensitive profile edits set a pending Admin-review marker; the shared backend will eventually store proposed and approved values separately.
- QueueStateCodec serializes the complete profile and clinic records for local process-safe persistence.
- Session schedules remain display strings in the local prototype. The backend stage will replace them with timezone-aware structured session records.
## Stage 5 appointment workflow and audit trail

- Appointment status changes are checked against explicit forward transition rules in DoctorViewModel; terminal COMPLETED and ABSENT records cannot be reopened locally.
- Call next remains a dedicated queue operation: it completes the current consultation, advances the next eligible token and records both operations.
- Every successful queue start, pause, resume, call-next, status update, consultation completion, day close and date rollover creates a QueueAuditEvent.
- Audit records include a monotonic local sequence, clinic date/time, Doctor or Assistant actor, token/patient context and before/after status when applicable.
- QueueStateCodec persists the newest 500 events in the local prototype. Invalid, unauthorized and no-op actions create no audit event.
- This local audit trail is for workflow testing. The shared backend must eventually assign authoritative IDs/timestamps and retain an immutable server-side log.

## Stage 5.1 recoverable queue order, walk-in booking and receipts

- Appointment.token is the permanent patient-facing number. Appointment.queueOrder is the mutable service order used by Call next.
- Skip removes a patient from eligible selection. Resume consultation now is allowed only when no other appointment is IN_CONSULTATION; Rejoin at end assigns the next queueOrder.
- A late BOOKED patient marked ARRIVED keeps the original token and is appended after the furthest progressed queue position.
- PATIENT_APP and CLINIC_WALK_IN appointments share the same daily token generator and persisted appointment collection.
- Arriving online patients and new clinic walk-ins receive a stable receiptNumber. Receipt generation and walk-in booking are permission checked and audited.
- AndroidTokenReceiptPrinter creates a one-page print document and opens the Android print service. Direct Bluetooth ESC/POS support remains behind TokenReceiptGateway until supported printer models and paper widths are selected.
- The backend must allocate token and queueOrder transactionally to prevent duplicate tokens when multiple clinic devices and the Patient App book concurrently.
