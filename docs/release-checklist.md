# DO-LO Doctor Release Checklist

## Current Stage 11 candidate

This checklist qualifies a local physical-device prototype. Passing it does not authorize production healthcare use.

## Automated gate

- GitHub Actions checks out the exact main-branch commit.
- JDK 17 and Gradle 8.9 are used.
- Android lintDebug passes.
- All debug unit tests pass.
- Debug APK assembly passes.
- Unsigned release assembly passes.
- APK checksum and debug artifact upload pass.

## Functional regression

- Doctor and every Assistant credential restore correctly after process death.
- Assistant permissions remain enforced in navigation and ViewModel actions.
- Morning and Evening queue states, tokens and closures remain independent.
- Fee-pending appointments cannot enter Queue.
- Compulsory receipt generation admits paid or waived appointments.
- Skip, late arrival, resume, absence and close/archive behavior remain correct.
- Clinic token limits, schedules and availability blocks remain enforced.
- Announcements, feedback, delay notices and sync events persist.
- Patient App future-booking policy persists and old clinic data migrates to current-day-only.
- Clinic walk-in booking remains current-day-only even when Patient future booking is enabled.
- Recurring full-day and single-session weekday closures persist and block only the intended session.
- Clearing a recurring closure restores normal eligibility automatically on open weekdays.

## Accessibility

Test at default font size and the largest practical Android font/display size:

- Home header and action icons do not overlap the Doctor name.
- All primary buttons and bottom navigation remain tappable.
- TalkBack announces page headings, metric label/value pairs, status badges, buttons and selected navigation.
- Dialogs receive focus, validation errors remain readable and keyboard actions do not hide confirmation controls.
- Light and dark themes retain readable contrast.
- Queue and payment state is communicated by text, not color alone.

## Security and privacy

- Manifest disallows backup and cleartext traffic.
- Mock-only APK has no Internet permission.
- No production secret, API key, signing key or real patient data is present.
- Demo credentials are clearly labeled and must be removed before production.
- Logs, receipts and screenshots are checked for unnecessary personal data.
- Production signing and Play integrity remain pending.
- A hosted backend security review is required before cross-device synchronization.

## Physical device matrix

Run at minimum on the previously used Vivo and Samsung devices:

- clean install;
- upgrade over Stage 10 without clearing data;
- process removal and relaunch;
- device restart and relaunch;
- light/dark theme;
- offline mode;
- 58 mm print preview and physical print when hardware is available.

Record APK version, Android version, device model, result and any screenshots in a release test note.