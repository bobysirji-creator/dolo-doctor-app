# DO-LO Doctor App

DO-LO Doctor is the dedicated lightweight Android app for doctors and permission-limited assistants in the DO-LO walk-in appointment ecosystem.

Current prototype: **0.5.4-stage5** (version code 13).

## Current prototype includes

- validated Doctor and Assistant mobile/PIN login with a settings-store primary session plus atomic-file and legacy preference recovery;
- Patient App-aligned light theme, saved low-glare dark theme, doctor dashboard and role-aware navigation;
- persisted Morning and Evening queue state, current token, queue date, appointment statuses and independent interactive queue controls;
- Doctor-only independent session closure, automatic next-date rollover and daily history finalized after both sessions close;
- pause/resume queue, call-next and permission-aware patient-status controls, including completion of the final active consultation;
- explicit appointment status-transition rules and a persistent Doctor activity log with actor, time, token/patient and before/after status context;
- recoverable skipped patients with immediate resume or end-of-queue rejoin, plus late-arrival requeueing without changing the original token;
- authorized clinic walk-in booking synchronized into the same queue, compulsory token-receipt generation, receipt reprints and Android system printing;
- session-aware booking that permits advance booking before a session starts, automatically closes each session at its configured end time, and reopens both on the next clinic day;
- explicit consultation-fee confirmation using Cash, UPI, Card, Online or Waived status; only paid/waived appointments with receipts enter Queue;
- independent Morning and Evening token sequences, persisted session selection, and centered 58 mm ESC/POS-oriented receipt output;
- editable, validated and persisted Doctor profile with Admin-review status for sensitive changes;
- persistent in-app notifications with an unread Home badge for queue, appointment, fee, receipt and session activity;
- editable, validated and persisted clinic contact details, session schedules, token limits and average consultation time;
- appointment availability blocks;
- doctor announcements, health camps and offers;
- assistant accounts with modular permissions, persistent Doctor-only deletion, and enforcement in both UI controls and ViewModel actions;
- editable-profile placeholders and Admin-review rules;
- backend-ready service interfaces;
- unit tests, Android lint and GitHub Actions APK builds.

All data is dummy local prototype data. There is no production authentication, healthcare backend, patient record, real message, payment or provider credential.

## Build without Android Studio

GitHub Actions builds the debug APK after every push to `main`. Open the latest successful Actions run and download `dolo-doctor-debug-apk`.

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
