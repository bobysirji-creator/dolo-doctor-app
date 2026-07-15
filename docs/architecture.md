## Stage 7 scheduled doctor updates

- Announcement records are validated as ISO date ranges and persisted in full under local schema version 4.
- AnnouncementPublicationStatus is derived as DRAFT, SCHEDULED, LIVE or EXPIRED; it is not stored as competing mutable state.
- PatientProfileFeedItem is the provider-neutral public projection. It exposes only live update content plus doctor/clinic identity needed by the Patient App.
- Home and patientProfileFeed exclude draft, future and expired records. Historical records remain available to the Doctor for editing or deletion.
- Announcement save, visibility and delete operations are permission checked and appended to the persisted audit/notification stream.
- AnnouncementPublisher and PatientProfileFeedGateway reserve shared-backend endpoints. Server time, clinic timezone, cross-device consistency and Patient App delivery remain backend responsibilities.

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

## Stage 5.2 independent consultation sessions

- DoctorUiState.sessionQueues stores separate Morning and Evening ConsultationQueue records. The older queueState/currentToken fields remain a Morning-session compatibility alias for migration from installed builds.
- Appointment.queueOrder and Appointment.token are calculated independently within the selected session; session plus token is the unique operational identity.
- Queue controls and Call next filter by session, so one session cannot progress, pause or complete the other session's patients.
- sessionBookingOpen parses the configured session end time and also checks the persisted maximum token capacity. It intentionally applies no start-time restriction, permitting advance booking, and rejects new bookings at or after the end time or at capacity.
- closeSession changes only the selected ConsultationQueue. The daily archive is produced only after both session queues are closed, preventing Morning closure from affecting Evening controls or booking.
- The appointments screen refreshes the date and cutoff state periodically. Date rollover archives the previous day and recreates both sessions as NOT_STARTED with token 0.
- Session cutoff is currently evaluated with device local time. The shared backend must eventually enforce the clinic timezone and cutoff atomically for both Patient App and clinic-device bookings.

## Stage 5.3 fee admission, token migration and 58 mm printing

- Appointment now persists consultationFee, paymentStatus, paymentMethod and paidAt. Queue eligibility requires a non-pending payment status plus a stable receipt number.
- confirmConsultationFee is permission checked, records FEE_CONFIRMED and RECEIPT_GENERATED audit events, changes BOOKED to ARRIVED and appends the patient to that session's service order.
- Call next and Queue UI filter out unpaid appointments even if they already hold an online-booking token.
- Token allocation takes the maximum only within the selected session. Receipt IDs include M or E so equal numeric tokens remain unambiguous.
- Local schema version 2 migrates an installed daily-global token list into independent per-session numbering, infers legacy receipts as paid, preserves the doctor's fee and upgrades receipt-capable assistants with the new confirmation permission.
- DoctorUiState.selectedSession is persisted and drives both Queue and Appointments instead of screen-local Compose state.
- AndroidTokenReceiptPrinter requests a 58 mm custom medium, zero margins and 203 dpi, then centers every rendered line on a 164-point receipt canvas. The output includes fee status, amount, method and confirmation time. Printer-service scaling and paper feed still require physical ESC/POS device validation.

## Stage 5.4 local notification projection

- The notification center projects persisted QueueAuditEvent records instead of maintaining a second conflicting event collection.
- DoctorUiState.notificationReadThrough stores the highest read audit sequence; SharedPreferences persistence keeps the unread badge stable through process recreation.
- The shared backend must eventually make fee confirmation, token allocation and receipt identity transactional and server-authoritative; no real payment gateway is connected in this local stage.

## Stage 6 availability enforcement

- AvailabilityBlock is a persisted clinic/date-range/session rule. `appointmentsEnabled = false` makes the rule active for Morning, Evening or Both.
- DoctorViewModel is the local enforcement boundary for walk-in booking, fee-confirmed queue admission and session booking eligibility; hiding a Compose control is not treated as authorization.
- Existing non-terminal appointments are linked to the active block and receive an AvailabilityImpactStatus rather than being deleted.
- CONTACT_PENDING, PATIENT_NOTIFIED and RESCHEDULE_REQUIRED appointments are withheld from Call next. RESOLVED appointments can re-enter normal queue progression.
- QueueStateCodec schema version 3 persists complete block records and per-appointment follow-up state while preserving older appointment formats.
- Availability save, enable/disable, delete and patient follow-up mutations create audit events that also drive the local notification center.
- AvailabilityManager reserves backend save, enable/disable, delete and affected-patient operations. The server must later evaluate clinic timezone/date, notify patients and resolve concurrent bookings transactionally.
