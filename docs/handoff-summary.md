## Stage 11.3 late-arrival cohorts and financial boundary

Version 0.11.3-stage11 (version code 22) groups late arrivals by the token that was in consultation when they were admitted. The first late patient receives the established position after up to four waiting patients. Additional late patients admitted during that same consultation join the same persisted cohort and are sorted by original token number, so token 6 cannot overtake token 5. A new active consultation creates a new cohort.

The Doctor App now states that consultation fees are collected directly by the clinic and merely recorded for queue admission and receipts. The misleading Online consultation-fee choice is no longer selectable; its enum value remains only for backward decoding of older local records.

DO-LO platform service charges are a separate future financial domain: the Patient App will pay a DO-LO service charge online during appointment booking, while doctors will receive weekly or monthly DO-LO billing based on the approved count/rate policy. Those ledgers, gateway transactions, invoices and controls belong to the shared backend and Admin App, not this Doctor App consultation-fee workflow.

Automated coverage is now 98 unit tests, including same-consultation late cohort ordering and persistence of the cohort anchor.

## Next recommended stage

Run the Stage 11.3 device checklist, especially consecutive late admissions while one consultation remains active. After acceptance, specify the hosted backend and Admin-controlled service-charge ledger before enabling any real payment provider.

## Stage 11.2 token-aware admission, dated reports and adaptive insets

Version 0.11.2-stage11 (version code 21) corrects Live queue admission order. Fee-confirmed online patients whose token is still ahead are inserted before higher future tokens, regardless of receipt-generation order. If their token turn has already passed, they retain the printed token and are placed after up to four waiting patients. The late-placement marker and resulting queue order persist across process recreation.

Operational summary now accepts an inclusive start/end date and defaults to the current clinic day. Queue History uses the same date-range interaction, includes today's live appointments before archive, and displays detailed Morning and Evening patient lists with status, source and fee.

All standalone screens now respect safe drawing insets, and the persistent bottom navigation explicitly clears Android gesture/three-button navigation areas. Automated coverage is 97 unit tests, including ordered online admission, four-patient late admission, inclusive report ranges and persistence codec coverage.

## Next recommended stage

Run the Stage 11.2 physical-device checklist on both gesture-navigation and three-button-navigation phones. After acceptance, the next major dependency is the hosted shared backend; local queue ordering must become a server transaction before Patient and Doctor devices synchronize in production.

## Stage 11.1 recurring weekly schedule

Version 0.11.1-stage11 (version code 20) adds Doctor-managed recurring weekday closures under Clinic & schedule. Morning and Evening can be disabled independently; selecting both creates a full-day clinic closure. Authorized Assistants retain read-only visibility.

The policy is persisted and included in shared clinic snapshots. Patient App booking validation evaluates the requested date, while clinic walk-ins, fee admission and queue start use the current clinic date. Exceptional holidays and leave remain date-range Availability blocks. Legacy clinic records migrate to an open weekly schedule.

GitHub Actions is the authoritative compile/lint/test gate. The next physical-device check should verify a full Sunday closure, a single-session closure, persistence after process death, and automatic availability on the next open weekday.
## Stage 11 future-booking policy and hardening

Version 0.11.0-stage11 (version code 19) adds Doctor-controlled Patient App future-booking policy to each clinic. The Doctor may keep current-day-only booking or allow 1 to 90 advance days. Existing installed clinic records migrate safely to current-day-only. Authorized Assistants can see the policy but cannot edit Clinic settings.

BookingPolicyEvaluator keeps booking channels separate. Patient App future dates follow the configured window; clinic walk-ins remain current-day-only under every setting. The local shared transport can validate a future date but does not mix it into the active day queue; scheduled appointments require the hosted backend.

Stage 11 also adds heading, metric and status semantics for TalkBack, removes a fixed Home header height for large fonts, removes unused Internet permission from the mock-only APK, keeps backup/cleartext blocked, and adds unsigned release assembly to CI. SECURITY.md and docs/release-checklist.md define the remaining production gates.

## Next recommended stage

Perform the Stage 11 physical-device and accessibility checklist. After acceptance, plan the hosted backend as a separate service before modifying either Android app for real cross-device data.
## Stage 10 shared backend contract and local mock transport

Version 0.10.0-stage10 (version code 18) adds a Doctor-only Shared Sync Center backed by a deterministic in-process transport. The Doctor can publish the current clinic-day snapshot, simulate a Patient App booking, and pull the latest shared revision. Revision matching rejects stale writes, and idempotency keys prevent duplicate publishes or appointments.

A simulated Patient App booking receives the next independent Morning or Evening token, remains BOOKED with fee PENDING, and has no queue order or receipt. Existing clinic fee confirmation and compulsory receipt generation then admit it to the operational queue. Shared revision, status, time and message persist locally; subsequent operational changes are marked PENDING.

This stage does not synchronize separate phones. The local transport resets with the app process and safely bootstraps again from the persisted revision. docs/shared-backend-contract.md defines the future HTTPS resources, transaction rules, conflict behavior and security gate. GitHub Actions remains the authoritative lint, unit-test and APK build environment.

## Next recommended stage

Stage 11: accessibility, security, automated UI coverage and release hardening. A real hosted backend should be planned as a separately deployable service before replacing the Stage 10 mock.
## Stage 9 reports, feedback and queue-delay readiness

Version 0.9.0-stage9 (version code 17) corrects Clinic access for Assistants. The Home Clinic card and navigation now accept VIEW_CLINIC or the previously granted MANAGE_CLINIC_AVAILABILITY permission. Authorized Assistants receive the complete clinic/schedule view without edit controls, while DoctorViewModel continues to reject Assistant clinic mutations. Local schema version 6 migrates the older availability permission to View clinic so installed Stage 8 choices keep working.

The new Reports workspace derives operational totals from current and archived appointments, including completed, absent, fee-pending and confirmed-fee metrics. It summarizes local patient ratings, allows authorized users to acknowledge individual feedback and persists that state with audit/notification events.

Doctors and Assistants with SEND_QUEUE_DELAY_NOTICE can create validated Morning or Evening delay notices from 5 to 240 minutes with a patient-facing message. Notices persist locally and appear in Reports; no real Patient App delivery occurs yet. OperationalReportGateway, PatientFeedbackGateway, QueueDelayNoticeGateway and ClinicPortfolioGateway reserve clinic-ID-scoped Stage 10 backend operations. Independent multi-clinic queue selection remains server-authoritative work because local queue state must not be duplicated unsafely.

GitHub Actions remains the authoritative compile, lint, unit-test and APK gate. Stage 9 physical-device acceptance is listed in docs/device-test-checklist.md.

## Next recommended stage

Stage 10: shared backend integration with the Patient App, beginning with a provider-neutral API contract and local mock transport before any live SMS, payment, maps or push provider.
## Stage 8 assistant accounts and credentials

Version 0.8.0-stage8 (version code 16) replaces the fixed-assistant-only prototype with a complete local Doctor-managed staff lifecycle. A Doctor can create an assistant with a validated name, unique 10-digit mobile number and selected initial permissions. The app generates a four-digit temporary PIN, displays it once and stores only a salted SHA-256 hash in the local credential registry.

Doctors can immediately disable or re-enable login, reset an assistant PIN, change each modular permission and permanently delete the profile and credentials. Assistant records, active status and permissions persist in local schema version 5. Disabled or deleted accounts cannot establish or restore a session. Existing Stage 2 demo assistants remain available as migration fixtures and can be managed through the same screen.

Creation, status, permission, PIN-reset and deletion changes are appended to the durable audit/notification stream. DoctorViewModel remains the local authorization boundary, while AssistantAdminGateway reserves shared-backend operations. Stage 14 now provides forced first-login PIN replacement locally. Production credential recovery, multi-device revocation and server authorization remain hosted-backend responsibilities.

GitHub Actions remains the authoritative compile, lint, unit-test and APK gate. Stage 8 physical-device acceptance is listed in docs/device-test-checklist.md.

## Next recommended stage

Stage 9: operational reports, patient feedback summaries, multi-clinic readiness and queue-delay notices.
## Stage 7 doctor updates and Patient App profile feed

Version 0.7.0-stage7 (version code 15) replaces the announcement demonstration toggles with complete doctor-update records. Doctors can create, edit, publish, move to draft and delete AVAILABILITY, CAMP, OFFER and GENERAL updates using validated ISO start/end dates, an 80-character title and a 300-character patient message.

Published records automatically evaluate as SCHEDULED, LIVE or EXPIRED against the clinic date. Draft, scheduled and expired records remain manageable in the Doctor App, while only LIVE records appear on Home and in the provider-neutral PatientProfileFeedItem projection. No real Patient App message is sent in this local stage.

Schema version 4 persists the full announcement collection and migrates Stage 6 active/hidden defaults. Save, visibility and delete actions create durable audit events and local notification-center entries. AnnouncementPublisher and PatientProfileFeedGateway reserve backend operations without embedding provider credentials.

GitHub Actions remains the authoritative compile, lint, unit-test and APK gate. Stage 7 physical-device acceptance is listed in docs/device-test-checklist.md.

## Next recommended stage

Stage 8: assistant creation, generated credentials and expanded permission administration, while keeping server authorization reserved for the shared backend.

## Stage 5.4 session closure, capacity and notifications

Version 0.5.4-stage5 (version code 13) corrects manual closure so the Doctor closes only the selected Morning or Evening queue. The other queue state, booking eligibility and appointments remain unchanged. An active consultation in the selected session is completed before closure. The dated DailyQueueHistory snapshot is finalized only after both sessions are closed.

The saved maximum-tokens-per-session value is now enforced through a single ViewModel capacity rule used by clinic walk-in validation and session booking availability. Today's appointments displays used/max capacity for both sessions and clearly marks LIMIT REACHED. Existing appointments are never deleted when a Doctor lowers the limit; additional bookings are blocked until the next clinic day.

A persistent notification center is available from the Home bell. Queue, appointment, fee, receipt and closure audit events appear newest first, unread events use a highlighted card, and the Home badge plus read-through position survive process recreation. This is the local operational notification stage; remote Patient App bookings, Admin broadcasts and Android push delivery still require the shared backend.

GitHub Actions remains the required compile, lint, unit-test and APK gate because Gradle/Android SDK are intentionally not installed on the low-resource development PC.
# Doctor App Handoff

## Stage 1 status

The repository contains a lightweight Kotlin and Jetpack Compose Doctor App skeleton using the same Android baseline as the Patient App. Doctor and Assistant demo roles share one APK but receive different dashboard access.

Implemented screens: splash, role selection/login, doctor dashboard, live queue, today's appointments, clinic schedule, availability, announcements, assistants/permissions and doctor profile.

The local queue demo can pause/resume, call the next patient and update basic appointment status. Announcement visibility, availability status and assistant permissions can be toggled in local memory. These controls demonstrate the workflow only and do not communicate with the Patient App.

Backend-ready contracts reserve doctor profile, clinic, appointment, queue, announcement, availability, assistant and notification operations. GitHub Actions runs lint, unit tests and debug APK assembly and uploads an APK plus checksum.

## Important limitations

- no production login or OTP;
- no shared backend or server authorization;
- no real patient or medical data;
- assistant deletions and the login session persist locally; other business-data changes are not persistent yet;
- no SMS, push, maps or payment providers;
- no Doctor/Admin broadcast delivery;
- no production signing.

## Next recommended stage

Stage 2: persisted local role/session, input validation and a permission-aware Assistant UI foundation before expanding editable doctor and clinic data.
## Stage 2 status

Version 0.2.0-stage2 (version code 2) replaces one-tap role selection with validated mobile-number and four-digit PIN credentials. Doctor, queue-enabled Assistant and view-only Assistant test identities are available. Successful sessions persist locally across app restarts and logout removes the stored session.

Assistant identity now selects an individual permission set. Queue visibility, appointment visibility, queue pause/resume, call-next, arrival and absence controls are permission-aware. The same permissions are checked again inside `DoctorViewModel`, so invoking an action without its required permission has no effect. Doctor-only profile, clinic, availability, announcements and staff routes remain inaccessible to Assistants.

Unit tests cover credential validation, role matching, Doctor actions, permitted Assistant actions, view-only restrictions and Doctor-only permission administration.

Next recommended stage: Stage 3 doctor profile, clinic and consultation schedule management with validated persisted edits and sensitive-field Admin-review flags.
## Stage 2 corrective release

Version 0.2.1-stage2 (version code 3) makes a restored local session automatically resume the saved Doctor or Assistant workspace after app relaunch. Session writes and logout clearing are committed synchronously so a rapid close/relaunch cannot race an asynchronous preference write.

Doctors can now delete an assistant from the local clinic workspace after an explicit confirmation. The ViewModel rejects the same operation when invoked by an Assistant. Removed assistant IDs persist locally and their credentials are rejected on later login attempts. Other assistant and business-data changes remain in-memory prototype behavior until the shared backend stage.
## Stage 2 device-lifecycle and theme release

Version 0.2.2-stage2 (version code 4) addresses physical-device reports from Vivo and Samsung phones. Authentication is reconstructed from an atomic internal session file, with SharedPreferences retained as a migration fallback. A restored session now selects the authenticated home route before the navigation graph starts, so removing the task from Android Recents does not intentionally route through login. Logout clears both persistence copies.

The light palette now matches the Patient App navy, teal, mint and neutral background colors. Cards, navigation, text and status surfaces use Material color roles instead of hard-coded white values. A moon/sun control on the dashboard enables a saved low-glare dark navy theme, including matching Android status and navigation bars.
## Stage 2 durable-session follow-up

Version 0.2.3-stage2 (version code 5) follows a second Vivo/Samsung physical-device test. Dark-mode persistence proved that `dolo_doctor_settings` survives both task finish and removal from Android Recents on those devices. The authenticated session payload is now stored primarily in that same proven preference file. The atomic file and original auth preferences remain fallback and migration sources. All three copies are cleared only by the explicit repository logout operation.

The Home logout icon now opens a confirmation dialog. Android Back and task removal do not call logout or clear any session store.
## Stage 2 workflow-state correction

Version 0.2.4-stage2 (version code 6) clarifies the physical-device report: authentication was retained, but `DoctorViewModel` recreated `DummyData` after process death. A dedicated `SharedPreferencesDoctorStateStore` now restores and saves the current token, queue status, every appointment status, active announcements, appointment-availability toggles and assistant permissions. Every permitted mutation is committed immediately. Logout changes authentication only and does not reset the clinic workflow.

A ViewModel recreation unit test advances the queue, changes an announcement, constructs a new ViewModel with the same store and verifies the operational state is restored.
## Stage 3 daily queue lifecycle

Version 0.3.0-stage3 (version code 7) assigns every local queue an ISO clinic date. A Doctor can use **Close and archive day** from Live queue after confirming the irreversible daily close. The queue becomes read-only and its current token plus every appointment and final status are stored as a dated DailyQueueHistory snapshot.

When the app restores a queue whose date is earlier than the device date, it archives that queue if necessary and starts a new empty NOT_STARTED queue with token 0. The rollover runs during ViewModel creation, login and queue mutations so it also protects an app left open across midnight. Logout continues to clear authentication only and never deletes clinic workflow history.

SharedPreferencesDoctorStateStore now persists the queue date, full current appointment list and encoded daily history. Missing keys migrate from the Stage 2 status-only format. The Doctor dashboard exposes Queue history; assistants cannot close a day or open Doctor-only history.

Unit coverage now includes manual close, post-close mutation blocking, unauthorized Assistant close, automatic next-date rollover, ViewModel recreation and lossless appointment/history encoding.

Next recommended stage: editable doctor profile, clinic and consultation schedules with validation, persistence and Admin-review flags for sensitive profile fields.
## Stage 4 profile, clinic and schedule management

Version 0.4.0-stage4 (version code 8) makes the Doctor profile and clinic schedule forms functional. Doctors can update name, specialty, qualification, registration number, experience, consultation fee and About. Name, fee, experience and About apply immediately; changes to specialty, qualifications or registration number set ProfileReviewStatus.PENDING_REVIEW so the future Admin workflow can approve sensitive public-profile changes.

Clinic editing now validates and saves clinic name, address, phone, morning and evening session text, maximum tokens per session and average consultation minutes. SharedPreferencesDoctorStateStore persists the full profile and clinic collection through lossless encoded records, with Stage 3 installations migrating from their existing defaults.

ViewModel validation rejects unauthorized Assistant calls, missing or short identity fields, invalid numeric ranges, invalid phone length and unknown clinic IDs. Compose forms show validation errors without closing and reload persisted values after process recreation.

The queue correction in this release changes Call next behavior when no later token exists: the current final IN_CONSULTATION appointment becomes COMPLETED, the button changes to **Complete consultation**, and a subsequent daily archive preserves that final status.

Next recommended stage: expanded appointment workflow and queue audit events, including explicit action history and status-transition rules.
## Stage 5 appointment workflow and audit trail

Version 0.5.0-stage5 (version code 9) adds explicit appointment status-transition validation. Invalid, terminal, unauthorized and no-op status changes are rejected without changing state or producing misleading audit records. Queue cards expose the legal local actions for booked, arrived, waiting, skipped and in-consultation patients, including a permission-aware Complete consultation control.

Every successful queue start, pause, resume, patient call, status change, consultation completion, manual day close and automatic date rollover now creates a persisted QueueAuditEvent. Each event records sequence, date/time, Doctor or Assistant actor, action, token/patient context, before/after status and a readable detail. The Doctor dashboard now exposes Clinic and Activity log as separate tools; the activity screen lists newest events first. Local history is capped at the latest 500 events pending the authoritative backend audit service.

Unit tests cover valid and invalid transitions, terminal-state protection, Doctor/Assistant attribution, queue lifecycle persistence and lossless audit-event encoding. GitHub Actions remains the build, lint and unit-test gate because the low-resource development PC does not host the Android toolchain.

Next recommended stage: Stage 6 appointment availability blocks and the affected-patient workflow.

## Stage 5.1 skipped/late recovery, clinic walk-ins and token receipts

Version 0.5.1-stage5 (version code 10) corrects the skipped-patient dead end. Token number and queue position are now separate: Resume consultation now safely reverses an accidental skip only when no other consultation is active, while Rejoin at end appends the same token to the remaining queue. Late patients marked arrived also keep their token and move to the end. Call next selects the lowest eligible queueOrder, so rejoined patients can be consulted even when their token is lower than the previously called token.

Appointments now record PATIENT_APP or CLINIC_WALK_IN source, mobile number, queue order and stable receipt number. Authorized Doctors and the queue-enabled Assistant can book an in-person patient from Today's appointments. A successful booking validates the form and session capacity, allots the next daily token, marks the patient ARRIVED, persists it in the shared local queue, records booking/receipt audit events and immediately opens a large-token receipt.

Every appointment exposes receipt generation or reprint. The receipt includes clinic, doctor, patient, token, date, session, source and receipt number. Print receipt uses Android's system print service and can save PDF or reach printers supported by an installed Android print service. Direct Bluetooth ESC/POS thermal printing is intentionally not hard-coded; TokenReceiptGateway reserves that provider integration once actual printer models are chosen.

Next recommended work remains Stage 6 availability blocks and affected-patient handling, followed by backend synchronization where token allocation and printing acknowledgements become authoritative.

## Stage 5.2 independent session queues and booking deadlines

Version 0.5.2-stage5 (version code 11) separates Morning and Evening into persisted ConsultationQueue records. Each session now has its own lifecycle state, current token, patient ordering, queue controls and Call next operation. Today's appointments and Live queue provide Morning/Evening selectors, and clinic walk-ins must join the selected session. Daily token numbers remain unique across both sessions.

New appointments are accepted before a session's configured start time, matching the advance-booking requirement. Morning automatically stops accepting bookings at its own end time while Evening remains available until its end time. There is no same-day automatic re-enable rule. At the next date rollover both queues reset to NOT_STARTED and accept bookings again until their respective cutoff. The appointments page refreshes cutoff/date state every minute, and ViewModel validation independently blocks stale UI submissions.

Existing Stage 5.1 receipt generation and Android system printing are unchanged. Physical thermal-printer output remains deferred until printer hardware/model and its Android print path can be tested.

Unit coverage includes independent queue advancement, per-session end-time boundaries, advance Evening booking, next-day reset/reopening, persistence across ViewModel recreation and session-queue codec round trips. Next recommended work remains Stage 6 availability blocks and affected-patient handling after this build passes GitHub Actions and physical-device session tests.

## Stage 5.3 consultation fee admission and independent session tokens

Version 0.5.3-stage5 (version code 12) adds an explicit clinic fee desk to Today's appointments. Online appointments with PENDING payment remain visible there but are excluded from Live queue. A Doctor or authorized Assistant confirms Cash, UPI, Card, Online or Waived status, records the amount/time, marks the patient arrived, assigns queue order, generates the receipt and only then admits the patient to Queue. Walk-in booking performs the same fee-confirmation and receipt steps in one form. Fee confirmation and receipt generation are separately audited and persisted.

Morning and Evening now allocate numeric tokens independently. Receipt IDs include M or E, for example DL-20260715-M-004 and DL-20260715-E-001. Installed Stage 5.2 state is migrated once to per-session numbering. The selected session is now ViewModel/store state shared by Queue and Appointments, so leaving either screen no longer resets it to Morning.

The Android receipt document is formatted for a 58 mm ESC/POS-oriented print service: custom paper width, requested 203 dpi, zero margins and centered content. It prints the session, token, fee status/amount, method and payment-confirmation time. This is still Android PrintManager PDF output; final centering, feed length and printer-driver behavior must be accepted on the user's physical 58 mm printer.

Unit coverage now includes unpaid queue exclusion, fee-confirmed admission, payment/receipt persistence, independent Morning/Evening allocation, selected-session restoration and expanded codec round trips. GitHub Actions remains the compile, lint and unit-test gate.

## Stage 6 appointment availability and affected-patient workflow

Version 0.6.0-stage6 (version code 14) replaces the former demonstration toggles with validated and persisted availability blocks. A Doctor can add, edit, reopen or delete a clinic block for an ISO date range and Morning, Evening or both sessions. An active block is enforced in both the UI and DoctorViewModel for clinic walk-ins, fee-confirmed admission and session booking availability.

Existing non-terminal appointments covered by an active block are retained and marked CONTACT_PENDING. The Doctor can record PATIENT_NOTIFIED, RESCHEDULE_REQUIRED or RESOLVED follow-up. Unresolved affected appointments remain visible but cannot be marked arrived or called into consultation; resolved appointments may continue in the existing queue. Availability and follow-up changes are persisted in schema version 3, audited and projected into the local notification center.

The Home header now places theme, notification and logout actions in their own upper row. The Doctor or Assistant name receives full width below it and no longer wraps because it is competing with three icons.

GitHub Actions remains the authoritative compile, lint, unit-test and APK gate because the local low-resource PC intentionally has no Android toolchain.

## Next recommended stage

Stage 7: make doctor announcements, camps, offers and availability notices into validated, expiring records and define the Patient App profile-feed contract. Real cross-app delivery still waits for the shared backend.

Stage 6 physical-device acceptance is defined in `docs/device-test-checklist.md`.

## Stage 12 hosted-backend readiness boundary

Version 0.12.0-stage12 (version code 23) separates the accepted local simulator from the future production transport. SharedBackendConfiguration declares LOCAL_MOCK or REMOTE_DISABLED mode, validates HTTPS-only endpoint configuration and rejects any attempt to enable SMS, DO-LO service-charge payments, maps or push before backend/Admin approval.

RemoteDisabledSharedBackendGateway is deliberately fail-closed: publish, pull and Patient booking operations return a non-retryable explanation and perform no network I/O. The default factory still creates LocalMockSharedBackendGateway, so all previously accepted single-device workflows remain available.

Shared Sync Center now displays transport readiness, cross-device state, external-provider OFF status and the production blockers. The manifest still has no INTERNET permission. Unit coverage is 101 tests, including default-mode, configuration-safety and locked-remote behavior. GitHub Actions remains the authoritative JDK 17/Android SDK compile, lint, test and APK gate.

Next recommended work is outside this Android repository: design and deploy the shared hosted API using docs/shared-backend-contract.md, then complete its authentication/authorization, transaction, audit, privacy and threat-model review. Do not add real provider keys or enable Android Internet access before that gate passes.

## Stage 12.1 stable prototype signing

Version 0.12.1-stage12 (version code 24) fixes Android's App not installed result for future updates. The cause was GitHub's fresh hosted runner creating a different default debug certificate for each build. Main-branch and manual CI builds now require four protected repository secrets, reconstruct a private PKCS#12 only in the runner temporary directory, sign both APK variants and verify that the APK certificate digest matches the keystore.

The artifact is now dolo-doctor-stable-debug-apk and includes APK, APK checksum and signing-certificate SHA-256 files. Pull requests receive no signing material and publish no installable artifact. No key, password or base64 value exists in this repository.

The initial stable APK still cannot update an APK signed by an old ephemeral key. After the repository secrets are saved and CI passes, the user must uninstall once, losing existing local prototype data, then install the stable APK. Every later APK using the protected key can update in place. The private setup bundle and individual secret-value files are stored outside the repository under C:\Users\Poly\Documents\codex\private\dolo-doctor-prototype-signing and must be securely backed up.

## Stage 13 encrypted backup and recovery

Version 0.13.0-stage13 (version code 25) adds a Doctor-only Backup & recovery workspace. It exports a portable `.dolo` file protected by PBKDF2-HMAC-SHA256 (150,000 iterations) and AES-256-GCM with a random salt and nonce. The authenticated encryption rejects incorrect passwords and modified files.

The backup includes doctor profile, clinics and schedules, current Morning/Evening queues, appointments, archive history, audit events, announcements, availability blocks, feedback and queue-delay notices. It deliberately excludes the signed-in session, Doctor/Assistant PIN hashes, assistant authentication records, signing keys and integration credentials. Restore retains the current device's authenticated role and assistant registry, resets shared-sync metadata to local-only, and requires a destructive-replacement confirmation.

GitHub Actions remains the authoritative compile, lint, unit-test and APK gate. Physical acceptance must first create a backup, change operational data, restore the backup, then build the next stable-signed APK and install it over this version without uninstalling to prove both recovery and in-place data retention.

## Next recommended stage

Design and security-review the separately deployed hosted API and Admin-owned service-charge ledger. Do not add Android Internet permission or real external providers until server authentication, authorization, atomic token allocation, audit and privacy policies pass.
## Stage 14 local credential hardening

Version 0.14.0-stage14 (version code 26) makes local PIN ownership functional. Doctors and Assistants can change their PIN from Home after proving the current PIN. New Assistant accounts and Doctor-issued PIN resets are persisted with a mandatory-change flag; login routes those Assistants directly to a non-bypassable replacement screen before any clinic tools become available.

Four-digit format, confirmation, difference from the current PIN and a local predictable-PIN denylist are enforced. Session and Assistant credential codecs accept both the new flag-bearing format and existing Stage 13 records, so stable in-place upgrades retain all current logins without unexpectedly forcing legacy users through migration. The requirement is refreshed from the credential registry during process restoration, preventing a stale saved session from bypassing a later Doctor PIN reset.

Doctor PIN changes persist locally across stable-signed updates but are excluded from clinic-data backups. There is no local Doctor PIN recovery in this prototype, so the UI tells the Doctor to record it securely. Assistants can be recovered through the existing Doctor reset flow. Server rate limiting, identity recovery, forced revocation and multi-device sessions remain hosted-backend responsibilities.

Automated coverage is now 111 unit tests. GitHub Actions remains the authoritative compile, lint, test, signing and APK gate.
## Stage 16D implementation checkpoint

Doctor App `0.15.0-stage16d` (version code 27) adds an explicit hosted staff queue without replacing the accepted local Doctor/Assistant workflow. The screen obtains a fixed seeded Doctor or queue-enabled Assistant session using demo PIN `1234`, stores renewable tokens encrypted by Android Keystore, reads server clinic sessions/appointments/queues, confirms the clinic-direct consultation fee for receipt/admission, and invokes authoritative queue commands. It polls every 15 seconds only while visible.

Local profile, clinic, assistants, appointments, queue history, reports, announcements and backup data are never uploaded or overwritten. Doctor-fee payment remains direct at the clinic; Maps, platform Payments, SMS and Push providers remain disabled.

Stage 16D was accepted on 2026-07-20 after GitHub Actions passed and all physical-device checks in `docs/stage16d-device-test.md` succeeded, including stable in-place upgrade, Doctor and permitted-Assistant hosted access, authoritative fee admission and queue commands, Patient App live updates, restart restoration, polling idempotency, local-data isolation, offline safety and reconnection recovery.