# DO-LO Doctor App

DO-LO Doctor is the dedicated lightweight Android app for doctors and permission-limited assistants in the DO-LO walk-in appointment ecosystem.

Current prototype: **0.16.0-stage16f** (version code 28).

## Current prototype includes

- validated Doctor and Assistant mobile/PIN login with a settings-store primary session plus atomic-file and legacy preference recovery;
- Patient App-aligned light theme, saved low-glare dark theme, doctor dashboard and role-aware navigation;
- persisted Morning and Evening queue state, current token, queue date, appointment statuses and independent interactive queue controls;
- Doctor-only independent session closure, automatic next-date rollover and daily history finalized after both sessions close;
- pause/resume queue, call-next and permission-aware patient-status controls, including completion of the final active consultation;
- explicit appointment status-transition rules and a persistent Doctor activity log with actor, time, token/patient and before/after status context;
- token-aware admission that restores upcoming token order, plus a persistent four-patient late-arrival buffer whose same-consultation cohort is served by original token number;
- authorized clinic walk-in booking synchronized into the same queue, compulsory token-receipt generation, receipt reprints and Android system printing;
- session-aware booking that permits advance booking before a session starts, automatically closes each session at its configured end time, and reopens both on the next clinic day;
- clinic-direct consultation-fee recording using Cash, UPI, Card or Waived status; DO-LO does not process the doctor's fee, and only clinic-confirmed/waived appointments with receipts enter Queue;
- independent Morning and Evening token sequences, persisted session selection, and centered 58 mm ESC/POS-oriented receipt output;
- editable, validated and persisted Doctor profile with Admin-review status for sensitive changes;
- persistent in-app notifications with an unread Home badge for queue, appointment, fee, receipt and session activity;
- editable, validated and persisted clinic contact details, session schedules, token limits and average consultation time;
- persisted appointment availability blocks for date ranges and Morning, Evening or both sessions, with affected-patient follow-up and queue safeguards;
- validated and persisted doctor announcements, availability notices, health camps and offers with draft, scheduled, live and expired states plus a Patient App feed contract;
- Doctor-managed assistant creation with individual mobile/PIN credentials, salted PIN hashes, instant enable/disable, PIN reset and persistent deletion;
- modular assistant permissions enforced in both UI controls and ViewModel actions, with provider-neutral server-authorization contracts;
- editable-profile placeholders and Admin-review rules;
- permission-aware read-only Clinic access for authorized Assistants while edits remain Doctor-only;
- inclusive date-range operational reports, detailed live/archive Morning/Evening queue history, patient-feedback acknowledgement and persisted queue-delay notices;
- ID-scoped multi-clinic data and provider-neutral service interfaces ready for shared-backend synchronization;
- Doctor-only Shared Sync Center with revision conflicts, idempotent commands, local publish/pull simulation and persisted sync status;
- simulated Patient App bookings with independent session tokens and fee-pending receipt/queue admission safeguards;
- Doctor-controlled Patient App future-booking policy: current-day-only or a validated 1-90 day advance window;
- recurring weekday schedule with independent Morning/Evening closures or a full clinic day off, enforced for online and walk-in bookings;
- permanent current-day-only clinic walk-ins, independent of the Patient App policy;
- explicit local-mock/locked-remote backend modes, HTTPS configuration validation and an in-app readiness checklist that cannot activate network or external providers;
- TalkBack semantics, large-font and system-navigation-inset layout protection, least-privilege mock manifest and CI compilation;
- an explicit financial boundary reserving Patient App booking service charges and doctor weekly/monthly platform billing for the shared backend and Admin App;
- stable secret-backed prototype APK signing with certificate verification for reliable future in-place updates;
- Doctor-only password-encrypted portable backup and restore for clinic workflow data, with credentials and secrets excluded;
- Doctor and Assistant PIN change, mandatory replacement of newly issued/reset temporary Assistant PINs, weak-PIN rejection and backward-compatible session migration;
- unit tests, Android lint and GitHub Actions APK builds.

All data remains dummy prototype data. Stage 16D adds a separate HTTPS connection for fixed seeded Doctor/Assistant identities and the authoritative prototype queue. Existing local clinic data is never uploaded. There is no production authentication, real patient record, real message, service-charge transaction or provider credential. Consultation-fee values remain clinic-recorded workflow data only.

## Build without Android Studio

GitHub Actions builds the debug APK after every push to `main`. Open the latest successful Actions run and download `dolo-doctor-stable-debug-apk`. See `docs/prototype-signing.md` for the one-time protected-secret setup and first-install warning.

## Local build requirements

- JDK 17
- Android SDK 35
- Gradle 8.9

```powershell
gradle --no-daemon :app:lintDebug :app:testDebugUnitTest :app:assembleDebug
```

## Demo flow

1. Open the app, choose Doctor or Assistant, and enter one of the on-screen demo credentials.
2. Doctor login exposes queue, appointments, clinic, availability, announcements, assistants and profile.
3. The queue-enabled and view-only Assistant accounts demonstrate different per-user permissions.
4. Restart the app to confirm the local session is restored, then use Logout to clear it.
5. Queue controls operate on local prototype data and resume their latest saved state after relaunch.
6. Open **Activity log** to inspect successful queue actions and confirm invalid or unauthorized actions create no event.
7. Close Morning and Evening independently; the complete dated snapshot appears under **Queue history** after both sessions close.

See `docs/roadmap.md` and `docs/handoff-summary.md` for staged development.
## Stage 16D hosted staff queue

Home now exposes a separate **Hosted Stage 16F queue** for the local Doctor and queue-enabled Assistant. It uses demo PIN `1234`, encrypts renewable hosted tokens with Android Keystore, and polls the seeded server queue every 15 seconds while visible. Staff can confirm the clinic-direct fee and admit an online appointment, start/pause/resume the session, call the next patient, and complete/skip/resume/mark absent through the existing protected API. The mature local queue, appointments, backup and reports remain separate and unchanged.
## Stage 16F hosted Assistant access

The hosted Doctor screen now lists the seeded server Assistant and can authoritatively enable/disable the account or change queue and clinic-fee permissions. The local Assistant registry and clinic workflow remain separate and unchanged. Real onboarding and all external providers remain disabled.

## Stage 18B hosted announcements

The seeded hosted Doctor can manage in-app announcements for Patient profiles. Hosted Assistants cannot access this surface. The mature local announcement workflow remains separate; no local data is uploaded and external providers remain disabled.

## Stage 20B hosted clinic schedule

The separate hosted Doctor workspace now includes an authoritative clinic-schedule editor. The seeded Doctor can set current-day-only or 1-90 day advance booking, the 1-30 day missed-appointment window, recurring Morning/Evening rules for every weekday, per-session token limits and average consultation time, plus date-specific whole-day or per-session closure/reopening. Hosted Assistants cannot see this editor. The accepted local clinic workflow and all local data remain isolated; Maps, Payments, SMS and Push remain disabled.