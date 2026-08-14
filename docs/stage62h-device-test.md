# Stage 62H Doctor drawer, notifications and back-stack device test

Build: Doctor App `0.30.0-stage62h` (version code 43)

## Upgrade and persistence

- [ ] GitHub Actions is green and the stable APK artifact is produced.
- [ ] The stable APK installs over the existing Doctor App.
- [ ] Existing login, clinic, assistants, permissions, appointments, queues, history, theme and hosted data remain present.

## Brand and Home identity

- [ ] A compact DO-LO Doctor logo appears above the Doctor/Assistant name on Home.
- [ ] The logo size and styling match the logo used on other Doctor App page headers.
- [ ] The circular white profile-image placeholder is larger and keeps the face/icon clearly visible.
- [ ] The notification bell remains at the top and no theme or logout icon appears there.

## Overlay navigation drawer

- [ ] Tapping the top-left hamburger overlays a drawer above the current Home screen instead of opening a separate More page.
- [ ] Android Back dismisses an open drawer without leaving Home.
- [ ] Tapping outside the drawer dismisses it.
- [ ] Day/Night mode works inside the drawer and remains persisted after restart.
- [ ] Logout is available inside the drawer.
- [ ] Doctor sees all permitted destinations.
- [ ] Assistant sees only permission-backed destinations and no Doctor-only profile, assistants, audit, backup or local-sync controls.

## Notifications versus Activity log

1. Perform local actions such as Start queue, confirm fee, generate receipt, Call next and Complete.
2. Open Notifications, then separately open Activity log from the drawer.

- [ ] Routine local actions do not create bell badges or cards in Notifications.
- [ ] The same routine actions remain recorded in Activity log.
- [ ] Real hosted notifications still appear, contribute to the bell badge and can be marked read.
- [ ] When there is no real notification, the Notifications screen displays an appropriate empty state.

## Back-stack safety

- [ ] From any authenticated child page, Android Back returns normally.
- [ ] From Home, Android Back closes/exits the Doctor App.
- [ ] Login again, visit several pages, open the drawer and Logout.
- [ ] The Login screen appears immediately after logout.
- [ ] Pressing Android Back from Login does not reveal Home, Queue, Appointments, Clinic or the last visited authenticated page.
- [ ] Relaunch after logout remains at Login.

## Regression

- [ ] Home, Appointments and Clinic bottom navigation remains correct.
- [ ] Stage 62G late-token behavior remains correct: consulting a lower late token never moves Last token backward.
- [ ] Morning/Evening sessions, compulsory receipt, clinic-direct fee confirmation, notifications, reports, backup and hosted workspace remain functional.

Record failures with phone model, Android version, role, theme, starting page and screenshot.