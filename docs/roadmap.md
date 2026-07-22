# DO-LO Doctor App Roadmap

- [x] Stage 1 - lightweight Compose project, Doctor/Assistant login, navigation and feature skeletons
- [x] Stage 2 - process-safe authentication and workflow state, Patient-aligned light/dark themes, permission-aware assistant access and Doctor-only removal
- [x] Stage 3 - dated queue lifecycle, Doctor-only close-day archival, automatic next-day reset and persisted appointment history
- [x] Stage 4 - validated, persisted doctor profile, clinic and consultation schedule management
- [x] Stage 5 - expanded appointment workflow with queue audit events
  - [x] Stage 5.2 - independent Morning/Evening queues and automatic session booking cutoffs
  - [x] Stage 5.3 - fee-confirmed queue admission, independent session tokens and 58 mm receipt layout
  - [x] Stage 5.4 - independent session closure, enforced token capacity and persistent in-app notifications
- [x] Stage 6 - appointment availability blocks and affected-patient workflow
  - [x] persisted date-range and Morning/Evening/Both booking controls
  - [x] affected-patient follow-up states and queue safety enforcement
  - [x] availability audit/notification events and backend-ready contracts
- [x] Stage 7 - announcements, camps, offers and Patient App profile feed contracts
  - [x] validated create, edit, publish, draft and delete workflow
  - [x] persisted scheduling with live/expired Patient profile visibility
  - [x] audit notifications and provider-neutral cross-app feed contracts
- [x] Stage 8 - assistant creation, credentials and backend-ready permission administration
  - [x] persisted Doctor-only assistant creation, enable/disable and deletion
  - [x] generated temporary PINs stored only as salted hashes with reset support
  - [x] modular permission administration, audit events and backend service contracts
- [x] Stage 9 - reports, feedback, multi-clinic readiness and queue-delay notices
  - [x] Assistant Clinic-access correction with read-only permission enforcement
  - [x] persisted operational metrics, feedback acknowledgement and delay notices
  - [x] clinic-ID-scoped reporting and backend integration contracts
- [x] Stage 10 - shared backend integration contract and local mock transport
  - [x] revision and idempotency-safe clinic snapshots
  - [x] Doctor-only Sync Center with publish/pull state
  - [x] simulated fee-pending Patient App booking with independent session token
- [x] Stage 11 - future-booking control, accessibility, security, tests and release hardening
  - [x] Patient App current-day/advance booking policy with 1-90 day limit
  - [x] clinic walk-ins permanently restricted to current day
  - [x] TalkBack semantics, large-font layout fixes and least-privilege manifest
  - [x] unsigned release compile gate and production security checklist
- [x] Stage 11.1 - recurring weekly clinic schedule
  - [x] independent Morning/Evening or full-day weekday closures
  - [x] shared booking, walk-in, fee-admission and queue-start enforcement
  - [x] backward-compatible persistence and physical-device checklist

- [x] Stage 11.2 - deterministic queue admission, date-range operations and adaptive screen insets
  - [x] token-order correction for on-time online admission
  - [x] stable four-patient buffer for late arrivals
  - [x] inclusive dated reports and detailed current/archive queue history
  - [x] safe drawing and navigation-bar inset handling

- [x] Stage 11.3 - same-consultation late cohorts and financial-domain separation
  - [x] persisted active-token cohort anchor for late admissions
  - [x] immutable token ordering inside each late-arrival cohort
  - [x] clinic-direct consultation-fee copy and UI clarification
  - [x] Admin-owned patient and doctor service-charge architecture

Admin App remains a separate future repository. Real providers remain disabled until the shared backend and policies are approved.

- [x] Stage 12 - hosted-backend readiness boundary
  - [x] explicit local-mock and locked-remote transport modes
  - [x] HTTPS-only configuration and external-provider safety validation
  - [x] in-app backend readiness/blocker display
  - [x] fail-closed remote gateway with no network implementation or Android Internet permission

Next dependency: build and security-review the hosted API as a separate service. Only after its authentication, authorization, atomic token allocation, audit and environment policies pass should a later Doctor App release add real HTTPS transport.

- [x] Stage 12.1 - stable prototype update signing
  - [x] encrypted GitHub Actions repository-secret contract
  - [x] PKCS#12 signing for debug and release artifacts on main
  - [x] APK/keystore certificate-digest verification
  - [x] private local backup bundle and one-time migration documentation

- [x] Stage 13 - encrypted local backup and recovery
  - [x] Doctor-only portable AES-GCM backup with password-derived encryption
  - [x] clinic, queue, appointment, history, report and communication data coverage
  - [x] authenticated restore with tamper/wrong-password rejection and explicit replacement confirmation
  - [x] login credentials, assistant authentication, signing keys and provider secrets excluded

Next dependency remains the separately deployed hosted API. Stage 13 provides local disaster recovery while that production backend is designed and reviewed.
- [x] Stage 14 - local credential hardening
  - [x] Doctor and Assistant current-PIN-verified PIN change
  - [x] mandatory first-login replacement for newly created/reset Assistant temporary PINs
  - [x] predictable PIN rejection and confirmation validation
  - [x] backward-compatible credential/session codecs and process-safe requirement restoration
  - [x] clinic access blocked until a temporary Assistant PIN is replaced

Next dependency remains server-authoritative identity, throttling, recovery and multi-device revocation in the separately deployed hosted API.
## Stage 16D - Seeded hosted Doctor/Assistant queue

- [x] explicit separate hosted screen; no local-state upload or replacement
- [x] HTTPS-only Platform API endpoint and Android INTERNET permission
- [x] fixed seeded Doctor/Assistant login with demo PIN 1234
- [x] AES/GCM Android Keystore token storage and renewable access session
- [x] server sessions, appointments and authoritative queue snapshots
- [x] clinic-fee confirmation/receipt admission and queue commands
- [x] 15-second visible-screen refresh with offline-safe local fallback
- [x] GitHub Actions, stable APK upgrade and physical-device acceptance (2026-07-20)

Recommended after acceptance: connect the accepted Patient and Doctor APKs simultaneously and verify that Doctor admission/call-next/completion updates the Patient live queue end to end.
## Stage 16F - Hosted Assistant access management

- [x] Doctor-only hosted Assistant directory in the authoritative queue workspace
- [x] server-owned active status and granular queue/clinic-fee permissions
- [x] immediate hosted enforcement without uploading or replacing local Assistant data
- [x] safe refresh after each access update and encrypted hosted-session retention
- [x] GitHub Actions, stable APK upgrade and physical-device acceptance (2026-07-20)

Real staff onboarding, OTP recovery and production identity remain gated on Admin workflow, privacy and security approval.

## Stage 18B - Hosted in-app communications

- [x] Doctor-only hosted announcement editor for availability, camps, offers and general updates
- [x] authoritative list, create, edit, publish and draft operations with retry-safe commands
- [x] Assistant identities excluded from hosted announcement management
- [x] active Patient feed reads Doctor announcements and Admin broadcasts without uploading local data
- [x] GitHub Actions compile, lint and unit tests
- [x] stable APK in-place upgrade and cross-app physical-device acceptance

SMS and Push delivery remain disabled. Stage 18B is an in-app prototype boundary only.
## Stage 19B - reviewed hosted Doctor profile

- [x] Doctor-only approved profile workspace inside the hosted boundary
- [x] bounded name, registration, specialty, qualification, experience and about editor
- [x] pending revision status and safe replacement submission
- [x] retry-safe idempotency key retained until a successful response
- [x] local Doctor profile, clinic, queue, credentials and backup data remain isolated
- [x] hosted session is bound to the current local Doctor/Assistant role
- [x] JSON contract unit coverage and stable-signed version increment
- [x] GitHub Actions compile, lint, unit tests and stable APK
- [x] Doctor/Admin/Patient cross-app physical-device acceptance (2026-07-21)

Assistants cannot access this surface. Patient-facing profile values remain unchanged until the Admin approves the pending revision.

## Stage 20B - authoritative hosted clinic schedule

- [x] Doctor-only hosted schedule editor
- [x] current-day-only or 1-90 day future booking control
- [x] recurring weekday Morning/Evening rules and per-session closure
- [x] token limit and average consultation-time editing
- [x] whole-day or session-specific date exceptions
- [x] hosted JSON contract unit coverage and stable version increment
- [x] local and hosted data remain isolated; external providers remain disabled
- [x] GitHub Actions compile, lint, unit tests and stable APK
- [x] stable APK in-place upgrade
- [x] paired Doctor/Patient physical-device acceptance

## Stage 21B/22A cross-app companion

- [x] Stage 21B SELF/FAMILY names and tokens accepted in hosted appointments/queue
- [x] no local Patient family data consumed by Doctor App
- [x] existing ABSENT command provides server-authoritative reschedule eligibility
- [x] Stage 22A original/replacement status and lineage physical-device acceptance

No Doctor APK change is required for this checkpoint.
## Stage 23A clinic receipt companion

- [x] existing hosted fee confirmation generates the authoritative receipt record
- [x] PAID and WAIVED remain clinic-direct and separate from DO-LO service charges
- [ ] Patient receipt status/reference cross-app physical-device acceptance

No Doctor APK change is required for this checkpoint.