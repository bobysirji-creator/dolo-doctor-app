# DO-LO Doctor App

DO-LO Doctor is the dedicated lightweight Android app for doctors and permission-limited assistants in the DO-LO walk-in appointment ecosystem.

Current prototype: **0.2.1-stage2** (version code 3).

## Stage 2 includes

- validated Doctor and Assistant mobile/PIN login with persisted local sessions;
- doctor dashboard and role-aware navigation;
- today's appointments and interactive queue skeleton;
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
5. Queue controls still operate only on local dummy state in Stage 2.

See `docs/roadmap.md` and `docs/handoff-summary.md` for staged development.