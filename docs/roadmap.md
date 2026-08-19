# DO-LO Doctor App Roadmap
- Stage 63P-D: controlled-pilot Assistant lifecycle implemented; device verification pending.

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
- [x] hosted staff can explicitly confirm PAID or WAIVED before admission
- [x] PAID and WAIVED remain clinic-direct and separate from DO-LO service charges
- [x] Patient receipt status/reference cross-app physical-device acceptance

Use existing current-day PENDING appointments for this checkpoint; duplicate test bookings are not required. Future appointments remain ineligible for clinic fee confirmation.
## Stage 25C - published Patient review feed

- [x] Doctor-only owner-scoped hosted review endpoint consumption
- [x] read-only PUBLISHED review cards for the clinic
- [x] PENDING, HIDDEN and REJECTED states excluded
- [x] Assistant UI exclusion backed by API denial
- [x] JSON boundary regression coverage and stable version increment
- [x] local Doctor data remains isolated
- [x] GitHub Actions compile, lint, unit tests and stable APK
- [x] combined Admin/Doctor/Patient physical-device acceptance

External providers remain disabled.

## Stage 28B - authoritative hosted staff notifications

- [x] load clinic-scoped hosted notifications for matching Doctor or Assistant identity
- [x] include hosted unread events in the Home notification badge
- [x] show Patient name, token and bounded appointment/queue event copy
- [x] mark one or all hosted events through a server-owned cursor
- [x] keep Doctor and Assistant read cursors independent
- [x] retain local notification data separately
- [x] refresh hosted activity every 15 seconds while the matching role is connected
- [x] JSON parsing regression coverage and stable version increment
- [x] GitHub Actions compile, lint, unit tests and stable APK
- [x] stable APK in-place upgrade
- [x] paired Doctor/Assistant physical-device acceptance

Android Push, SMS, Maps and Payments remain disabled.

## Stage 37B - Doctor-targeted in-app messages

- [x] load authenticated Doctor-only Admin-approved campaign feed
- [x] require authoritative, in-app-only, provider-disabled metadata
- [x] dedicated targeted message section in hosted Doctor workspace
- [x] Assistant role receives no feed request, section or inherited messages
- [x] preserve last hosted snapshot and every local Doctor record during ordinary offline failures
- [x] parser and provider-rejection regression coverage
- [x] stable version increment
- [x] GitHub Actions compile, lint, unit tests and stable APK
- [x] API deployment and stable APK in-place upgrade
- [x] Stage 37 physical-device acceptance

SMS remains OTP-only and disabled; Push, Maps and Payments remain disabled.

## Stage 42B - public DO-LO identity

- [x] implementation and strict parser coverage
- [x] stable version increment
- [x] GitHub Actions stable APK
- [x] physical-device acceptance

## Stage 44B - clinic booking-account public identity

- [x] parse strict `DLO-PAT-NNNNNN` booking-account identity
- [x] parse explicit SELF/FAMILY relationship
- [x] display Patient DO-LO ID on every hosted appointment card
- [x] label family bookings as belonging to the booking account
- [x] reject cross-role IDs and unknown relationship values
- [x] preserve Doctor/Assistant role and permission boundaries
- [x] stable version increment and unit coverage
- [x] GitHub Actions compile, lint, unit tests and stable APK
- [x] stable APK in-place upgrade and Stage 44AB physical-device acceptance

Patient phone data and internal UUIDs are not displayed. External providers remain disabled.

## Stages 52-64 - production-capable ecosystem roadmap

The cross-app production sequence, dependencies and completion rules are maintained in docs/ecosystem-roadmap-52-64.md. App-specific implementation and acceptance checkpoints will be added here as each stage reaches this repository.


## Stage 61B-P - authoritative cache rehearsal

- [x] Android Keystore-encrypted hosted read cache with 24-hour maximum
- [x] visible live, cached-fresh and cached-stale status
- [x] one retry only for transient idempotent commands
- [x] HTTP 409 refresh-required recovery without overwrite
- [x] hosted cache purge on logout; Doctor cache also purges on role change
- [x] local prototype data remains separate and is never uploaded
- [x] version 0.26.0-stage61bp
- [ ] GitHub Actions and stable APK physical-device acceptance

## Stage 61B-P physical-device acceptance

On 11 August 2026, the complete Stage 61B-P device checklist passed across Patient, Doctor and Admin apps. Encrypted hosted-cache fallback, freshness labels, one bounded retry, conflict refresh, logout/role cache isolation and local-data separation are accepted for the controlled prototype.
## Stage 61B-P physical-device acceptance

The full Stage 61B-P device checklist passed on 11 August 2026.

## Stage 62B - Doctor information architecture

- [x] replace Home/Queue/Appointments/Profile navigation with Today/Appointments/Clinic/More
- [x] make queue control and current-day appointment intake the focus of Today
- [x] add a dedicated role-aware More workspace grouped into Account, Clinic management, and Insights and data
- [x] keep Assistant destinations hidden unless explicitly permitted
- [x] preserve Doctor-only profile, Assistant management, audit, backup and local-sync controls
- [x] keep queue, appointment, clinic-fee, receipt, session and hosted behavior unchanged
- [x] retain dark theme, notification access, safe logout and responsive bottom navigation
- [x] add pure navigation-policy regression tests
- [x] version 0.27.0-stage62b (version code 40)
- [ ] GitHub Actions and stable APK physical-device acceptance

Stage 62C will finish remaining Patient navigation and shared cross-app accessibility polish.
## Stage 62B physical-device acceptance

On 11 August 2026, Doctor App `0.27.0-stage62b` passed the complete physical-device checklist. Today, Appointments, Clinic and role-aware More navigation, Doctor/Assistant visibility rules, queue and appointment workflows, dark theme, notification access, restart persistence and local-data safety are accepted.
## Stage 62F - Doctor shared visual-system foundation

- [x] adopt the accepted Patient App day and night palette as the DO-LO ecosystem baseline
- [x] align Doctor typography and rounded shape tokens with the Patient App
- [x] replace teal/green primary actions with accessible DO-LO blue actions
- [x] flatten shared cards, fields, back controls and buttons in Day mode
- [x] retain layered elevation and high-contrast surfaces in Night mode
- [x] replace the custom Doctor bottom bar with accessible Material 3 navigation items
- [x] preserve Today, Appointments, Clinic and More information architecture
- [x] preserve Doctor/Assistant permissions, queue, sessions, receipts, reports and hosted synchronization
- [x] version 0.28.0-stage62f (version code 41)
- [x] GitHub Actions compile, lint, unit tests and stable APK (run 31507289180)
- [x] stable APK in-place upgrade and Stage 62F physical-device acceptance

The accepted Patient visual system is now the canonical baseline for Patient, Doctor and future Admin modernization. This stage changes presentation only; no database, API, provider or business-workflow contract changed.
## Stage 62F physical-device acceptance

On 14 August 2026, Doctor App `0.28.0-stage62f` passed the complete physical-device checklist. Shared Patient-theme adoption, Day/Night contrast, navigation semantics, role boundaries, queue/session/receipt workflows and persisted local data are accepted.
## Stage 62G - compact Doctor Home and monotonic queue progress

- [x] draw the Home background edge-to-edge behind the status area while retaining safe content insets
- [x] move theme and logout controls out of Home and retain them in More
- [x] add a top-left hamburger entry to More and keep Notifications at the top
- [x] add a white circular Doctor/Assistant image placeholder beside compact identity text
- [x] reduce Home padding, spacing and heading sizes for better first-screen information density
- [x] show Morning and Evening appointment totals instead of current tokens
- [x] replace the Live queue button with a top-right View action
- [x] remove the duplicate Today appointments card
- [x] use Home, Appointments and Clinic as the three persistent bottom destinations
- [x] shorten Start queue to Start and Complete consultation to Complete
- [x] keep Last token monotonic when a lower late/rejoined token is consulted
- [x] add regression coverage for late token 6 after progress token 15
- [x] preserve Assistant permissions, independent sessions, queue ordering, receipts and hosted/local isolation
- [x] version 0.29.0-stage62g (version code 42)
- [x] GitHub Actions compile, lint, unit tests and stable APK (run 31809548353)
- [ ] stable APK in-place upgrade and Stage 62G physical-device acceptance

`Last token` is now the public progress marker, not necessarily the token currently in consultation. The queue list remains the source for the actual active Patient. No API or persisted-schema migration was required.
## Stage 62H - Doctor drawer, notification separation and back-stack safety

- [x] restore the compact DO-LO Doctor logo above the Home identity
- [x] use the same compact logo component across Home, page headers, login, splash and the navigation drawer
- [x] enlarge the circular Doctor/Assistant profile-image placeholder
- [x] replace the full More page with a permission-aware overlay navigation drawer
- [x] retain Day/Night and Logout actions inside the drawer
- [x] keep the notification bell at the top of Home
- [x] exclude local audit events from notification counts and the Notifications screen
- [x] retain queue, fee, receipt, session and configuration actions exclusively in Activity log
- [x] clear the complete authenticated navigation graph on logout
- [x] make Android Back close the app from Home while retaining normal back behavior elsewhere
- [x] preserve Stage 62G monotonic queue progress and all persisted data
- [x] version 0.30.0-stage62h (version code 43)
- [x] GitHub Actions compile, lint, unit tests and stable APK (run 31823774406)
- [ ] stable APK in-place upgrade and Stage 62H physical-device acceptance

The drawer uses the existing role and permission policy, so Doctor-only destinations remain hidden from Assistants. No database, API or persisted-state migration is required.
## Stage 63P-B — Android controlled-pilot identity integration

- [x] configurable pilot API origin for all three Android CI builds
- [x] Admin pilot login and one-time Patient/Doctor invitation UI
- [x] Patient and Doctor invitation activation and DO-LO ID login
- [x] encrypted session restoration and role validation
- [x] role-bound invitation consumption before account creation
- [x] ordinary API authorization no longer depends on seeded UUIDs
- [x] unprovisioned Doctor fails safely without inheriting demo clinic data
- [x] combined deployment and physical-device checklist
- [ ] four GitHub Actions checks and pilot Render deployment
- [ ] three stable APK upgrades and Stage 63P-B device acceptance

Stage 63P-C will add authoritative Doctor profile/clinic onboarding and Admin verification so an invited Doctor can create the first real pilot clinic workspace. Open registration, Assistant pilot enrollment, real OTP, payments, and production traffic remain deferred.
## Stage 63P-C - Admin-reviewed first clinic onboarding

- [x] replace the controlled-pilot Doctor setup dead end with a complete first-profile and clinic form
- [x] collect bounded Doctor registration, specialty, qualification, experience and profile information
- [x] collect clinic identity, address, contact, fee, booking policy and weekly Morning/Evening sessions
- [x] preserve pending submissions as read-only and reopen rejected submissions with the Admin note
- [x] refresh an approved setup into the server-authoritative hosted clinic workspace
- [x] retain encrypted pilot session restoration and prohibit seeded clinic inheritance
- [x] add onboarding JSON contract coverage
- [x] version 0.32.0-stage63pc (version code 45)
- [ ] GitHub Actions compile, lint, unit tests and stable APK
- [ ] stable APK upgrade and Stage 63P-C physical-device acceptance