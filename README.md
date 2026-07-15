# DO-LO Doctor App

DO-LO Doctor is the dedicated lightweight Android app for doctors and permission-limited assistants in the DO-LO walk-in appointment ecosystem.

Current prototype: **0.3.0-stage3** (version code 7).

## Current prototype includes

- validated Doctor and Assistant mobile/PIN login with a settings-store primary session plus atomic-file and legacy preference recovery;
- Patient App-aligned light theme, saved low-glare dark theme, doctor dashboard and role-aware navigation;
- persisted current token, queue date, queue state, appointment statuses and interactive queue controls;
- Doctor-only close-day confirmation, automatic next-date rollover and persisted daily queue history;
- pause/resume queue, call-next and patient-status demo controls;
- clinic and consultation schedule screen;
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
6. Use **Close and archive day** to lock the queue and save its complete appointment snapshot under **Queue history**.

See `docs/roadmap.md` and `docs/handoff-summary.md` for staged development.