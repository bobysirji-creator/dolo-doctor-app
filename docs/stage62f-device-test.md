# Stage 62F Doctor visual-system device test

Build: Doctor App `0.28.0-stage62f` (version code 41)

## Upgrade and persistence

- [x] GitHub Actions is green and the stable APK artifact was produced.
- [ ] The stable APK installs over the existing Doctor App.
- [ ] Existing Doctor/Assistant login, clinic, appointments, queue state, settings and Dark Mode preference remain present.

## Day theme

- [ ] Day mode matches the accepted Patient App direction: ice-blue page background, white surfaces and light-blue primary actions.
- [ ] Shared dashboard, metric and section cards are flat without decorative blue borders or heavy shadows.
- [ ] Login fields and other standard fields remain readable and show a clear focused state.
- [ ] Primary actions are blue rather than green/teal and white labels remain readable.

## Night theme

- [ ] Night mode uses deep navy page and card layers with readable cool-white text.
- [ ] Cards, dialogs, fields and navigation remain visually separated without excessive brightness.
- [ ] Blue selected states and actions are consistent with the Patient App Night theme.
- [ ] Theme selection persists after closing and relaunching the App.

## Navigation and accessibility

- [ ] Today, Appointments, Clinic and More appear in the same order as before.
- [ ] The selected destination has a visible blue indicator and readable label.
- [ ] Doctor can open all four destinations.
- [ ] Assistant Clinic access remains disabled unless the required permission is granted.
- [ ] Large font and the phone navigation area do not hide bottom actions.

## Workflow regression

- [ ] Morning and Evening sessions retain independent state.
- [ ] Online and walk-in appointment intake, clinic fee confirmation and receipt generation still work.
- [ ] Queue start/pause/call-next/complete/skip/rejoin/close-and-archive behavior remains unchanged.
- [ ] Doctor-only actions remain absent for Assistant accounts.
- [ ] Notifications, reports, announcements, availability, backup and hosted workspace remain available according to role and permission.

Record failures with phone model, Android version, role, theme, screen and screenshot.