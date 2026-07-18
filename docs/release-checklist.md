# DO-LO Doctor Release Checklist

## Current Stage 12 candidate

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
- Queue, clinic-fee-record and future service-charge states are communicated by text, not color alone.

## Security and privacy

- Manifest disallows backup and cleartext traffic.
- Mock-only APK has no Internet permission.
- No production secret, API key, signing key or real patient data is present.
- Demo credentials are clearly labeled and must be removed before production.
- Logs, receipts and screenshots are checked for unnecessary personal data.
- Stable prototype signing is supplied only through protected GitHub Actions secrets and verified by certificate digest.
- Production signing, Play App Signing and Play integrity remain pending.
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

## Stage 12 backend-boundary acceptance

1. Upgrade over 0.11.3 without clearing data. Confirm login, queue, appointments, reports, clinic settings and late-arrival order are retained.
2. As Doctor, open Shared sync center and confirm Stage 12 shows Local mock transport and Local-only safe mode.
3. Confirm the screen reports hosted sync as Not enabled and SMS, DO-LO charges, Maps and Push as OFF.
4. Publish, simulate one Patient App booking and pull. Confirm the local simulator still behaves as before.
5. Put the phone in airplane mode and repeat the local simulator flow. Confirm it remains functional.
6. Inspect Android app permissions and confirm no network/Internet permission is requested.
7. Confirm there is no control that can select remote mode, enter an endpoint, or enable an external provider.
8. Re-run core Morning/Evening queue, fee/receipt, late cohort, archive and relaunch checks on Vivo and Samsung.

## Stage 12.1 stable-signing acceptance

1. Confirm all four DOLO_SIGNING repository secrets exist before pushing the workflow commit.
2. Confirm CI fails safely when a required secret is absent and never prints a secret value.
3. Confirm the successful artifact is named dolo-doctor-stable-debug-apk and contains signing-certificate.sha256.
4. Finish any old-installation checks, then uninstall the old ephemeral-key APK once and accept that its local data is erased.
5. Install the first stable APK and exercise login, queue and appointment workflows.
6. Build the next version with the same protected secrets and install it over the stable APK without uninstalling.
7. Confirm Android accepts the update and existing local application state remains present.
8. Compare signing-certificate.sha256 across stable builds and confirm it never changes.

## Stage 13 encrypted-backup acceptance

1. Install 0.13.0-stage13 over the current stable-signed APK without uninstalling and confirm all existing local state remains.
2. Login as Doctor, open Backup & recovery, enter matching passwords of at least eight characters and save a `.dolo` file.
3. Change a clinic field and at least one appointment/queue state, then restore the saved file and accept the replacement warning.
4. Confirm the prior clinic, appointment, queue, history, announcement and availability data return.
5. Confirm the current Doctor login and Assistant credentials were not replaced by the backup.
6. Try a wrong password and a non-`.dolo` file; confirm neither changes current data.
7. Keep the backup file private and store its password separately. Losing the password makes the backup unrecoverable.
8. Repeat in light/dark mode and at large font size on Vivo and Samsung devices.