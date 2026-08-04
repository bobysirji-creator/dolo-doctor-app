# DO-LO Ecosystem Development Roadmap - Stages 52 to 64

This is the cross-repository source of truth for moving the accepted seeded-dummy prototype to a production-capable Patient, Doctor and Admin ecosystem. A stage may not activate a provider, accept real identity data or weaken a fail-closed boundary unless its listed dependency and acceptance evidence are complete.

## Non-negotiable product rules

- SMS is used only for authentication OTP and essential account security, never promotions.
- Doctor consultation fees are paid directly at the clinic and remain outside DO-LO online payments.
- DO-LO charges the Patient per online appointment at booking.
- DO-LO bills the Doctor weekly or monthly only for online appointments; clinic walk-ins do not accrue Doctor platform charges.
- Admin may configure a date-bounded zero Doctor charge trial.
- Public DO-LO IDs remain server-owned and contain no phone, location, PIN or profile data.
- Doctor announcements appear on that Doctor's profile. Group targeting is Admin-owned.
- Production enrollment stays disabled until every activation gate is reviewed and satisfied.

## Stage 52 - immutable legal documents and consent presentation

### 52A - release foundation

- Empty immutable release table for Terms, Privacy and Health Data.
- SHA-256 content integrity, language/version metadata and approval reference requirements.
- Fail-closed publication controls and public readiness metadata.
- No legal content, Patient viewing, consent collection or enrollment activation.

### 52B-P - prototype legal-document viewer

- Serve three bounded, integrity-protected English test drafts outside the immutable production release table.
- Mark every document `DRAFT_TEST_ONLY` and visibly **TEST DRAFT - NOT LEGALLY APPROVED**.
- Patient App viewer supports small screens, dark mode and accessibility.
- Test acknowledgement is memory-only, creates no consent record and disappears when UI state is recreated.
- Production publication, consent collection, enrollment and every external provider remain disabled.

### 52B-F - approved publication and Patient viewer

- Insert only legally approved immutable document versions through a reviewed migration.
- Activate production read-only viewing with effective-date and language handling.
- Replace the test viewer source with approved, integrity-verified releases.
- Consent collection remains disabled until the acceptance transaction is reviewed.

Dependency: legally approved Terms, Privacy Notice and Health Data Consent content, version identifiers, effective dates and approval references.

## Stage 53A - fail-closed account-recovery policy foundation

- Record verified-mobile change, lost-device recovery, duplicate prevention/resolution and support ownership as explicit decisions.
- Require HMAC-SHA-256 identity lookup with a separately managed pepper; forbid plaintext phone lookup.
- Forbid support-agent credential bypass and automatic account merging.
- Accept no Patient recovery input and expose no recovery, lookup, merge or mutation action.

Completion gate: PostgreSQL-verified migration, hosted readiness checks and all decisions remain fail closed.

## Stage 53B-P - seeded dummy recovery simulation

- Seeded Patient submits only a predefined scenario and exact test-only acknowledgement.
- Seeded Admin owns review through predefined no-change or test-escalation commands.
- Persist immutable append-only event history without phone, identity evidence or free-text recovery data.
- Never change credentials, mobile numbers, account ownership or records; never merge accounts.

Completion gate: API/PostgreSQL verification, Patient and Admin stable builds, hosted checks and combined physical-device acceptance.

## Stage 53B-F - approved production recovery policy and governed UX

- Define approved evidence rules for mobile-number change and lost-device recovery.
- Assign accountable production support ownership and immutable exception auditing.
- Define manual duplicate-account escalation without automatic record merging.
- Add production Patient recovery UX only after policy, privacy, legal and security approval.

Dependency: approved recovery and duplicate-account policy plus production support ownership. Stages 53A and 53B-P do not satisfy this dependency.

## Stage 54A - fail-closed data lifecycle policy foundation

- Inventory required record classes without reading or returning record contents.
- Enumerate retention, correction, export, deletion, statutory-hold, anonymization, consent-withdrawal and audit decisions.
- Keep every retention duration undefined and every lifecycle action disabled.
- Preserve appointment, clinic-receipt, billing and audit integrity; expose no lifecycle mutation route.

Completion gate: PostgreSQL-verified migration, GitHub Actions, Render deployment and no-input hosted verification.

## Stage 54B-F - approved lifecycle requests and governed execution

- Patient correction/export/deletion and consent-withdrawal request workflow.
- Admin review, statutory hold, anonymization and immutable audit evidence.
- Approved per-record retention schedule and withdrawal effects without corrupting operational records.

Dependency: approved retention, correction, deletion, legal-hold, anonymization and consent-withdrawal policy plus accountable ownership. Stage 54A does not satisfy this dependency.

## Stage 55 - production infrastructure and security controls

- Approved India-region production compute/database topology.
- Managed secrets, verified TLS, private networking where possible and key rotation.
- Shared/distributed rate limiting for OTP, login, booking and privileged actions.
- Monitoring, incident response, encrypted backups and measured restore drills.
- Independent security review of production configuration.

Dependency: paid production hosting choices, security owner and approved operational region.

## Stage 56 - managed OTP and production authentication

- Select and integrate an OTP provider for authentication-only SMS.
- Challenge-bound codes, resend/attempt limits, abuse protection and delivery audit.
- Production sessions, refresh rotation, device management and logout/revocation.
- Patient OTP UX plus hardened Doctor/Assistant and Admin authentication.
- No promotional SMS capability.

Dependency: OTP provider account, approved sender/template, credentials in managed secrets and commercial/data-processing review.

## Stage 57 - atomic Patient enrollment

- One transaction verifies OTP, approved consent versions and duplicate policy.
- Allocate the server-owned DLO-PAT-NNNNNN ID exactly once.
- Create Patient profile and auditable consent receipts without storing plaintext OTP.
- Production family-member enrollment with guardian/age rules.
- Retry/idempotency and rollback tests across every failure point.

Dependency: Stages 52-56 accepted and all Patient-enrollment gates reviewed.

## Stage 58 - production Doctor, Assistant and Admin identities

- Real Doctor onboarding, credential/registration review and verified activation.
- Doctor-owned Assistant invitation, permission and revocation lifecycle.
- Hardened Admin enrollment with stronger authentication and recovery controls.
- Replace seeded-only identity restrictions while preserving role and clinic boundaries.

Dependency: onboarding evidence policy, Doctor verification ownership and Admin security policy.

## Stage 59 - platform payments and Doctor billing

- Patient pays only the Admin-defined DO-LO service charge during online booking.
- Payment order ownership, amount/currency binding and webhook-authoritative confirmation.
- Booking completion/expiry rules for successful, failed or abandoned service-charge payments.
- Doctor weekly/monthly invoice generation for online bookings only.
- Date-bounded zero-charge Doctor trials, reconciliation, refunds and disputes.
- Doctor consultation fee remains clinic-direct.

Dependency: payment gateway, merchant/KYC approval, webhook secrets, refund/settlement policy and accounting ownership.

## Stage 60 - Maps, Push and operational provider delivery

- Maps provider for clinic coordinates, distance and navigation.
- Push provider for queue, appointment, availability and security alerts.
- Transactional outbox, signed callbacks where applicable, retries and dead-letter handling.
- SMS remains OTP-only; broadcasts and promotions remain in-app/push according to consent.

Dependency: Maps and Push provider projects, keys in managed secrets, quotas and approved message policy.

## Stage 61 - authoritative ecosystem cutover

- Replace remaining seeded/local authoritative paths with production API data.
- Patient, Doctor and Admin apps share real account, clinic, appointment, queue and billing state.
- Offline read cache, bounded command retry, idempotency and conflict recovery.
- Controlled migration of prototype configuration only; no dummy identities in production.
- Cross-app end-to-end tests for online/offline booking, fee receipt, sessions and queue order.

Dependency: production identities and core providers accepted.

## Stage 62 - complete product UX modernization

- Finish remaining Patient screens and navigation polish.
- Complete Doctor Stages 46A-46B information architecture and workflows.
- Complete Admin Stages 47A-47B People, Communications, Billing and Operations navigation.
- Shared accessibility, large text, small/large screens, dark mode, reduced motion and clear offline/error states.

Dependency: stable production contracts to avoid redesign churn.

## Stage 63 - pilot, security and release candidate

- Automated API, migration, permission, Android and end-to-end regression suites.
- Load/concurrency tests for booking, queue and OTP boundaries.
- Penetration/security review, dependency and secret scanning.
- Backup/restore and incident-response exercises.
- Controlled pilot with one or more real clinics under approved data handling.
- Production signing, privacy disclosures and Play Store release candidate.

Dependency: pilot clinics, testers, security review and Play Console readiness.

## Stage 64 - production launch and operations

- Final go-live review, migration freeze, rollback plan and production capability switch.
- Monitoring dashboards, support escalation, billing reconciliation and provider alerts.
- Phased rollout with health/error thresholds and emergency disable controls.
- Post-launch audit and backlog for the richer web-based Admin console.
- Remove prototype-only controls and document the supported production baseline.

Dependency: signed operational, legal, security and business launch approval.

## Completion definition

The Android ecosystem is production-capable after Stage 64 only when every required dependency is supplied, all hosted/physical-device checks pass, real-data handling is approved, external providers are monitored, and rollback/recovery evidence is current. Passing a code test alone does not satisfy legal, security, provider or operational gates.