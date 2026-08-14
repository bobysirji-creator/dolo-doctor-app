# Stage 62G Doctor compact-Home and queue-progress device test

Build: Doctor App `0.29.0-stage62g` (version code 42)

## Upgrade and persistence

- [ ] GitHub Actions is green and the stable APK installs over the existing Doctor App.
- [ ] Existing login, clinic, assistants, permissions, appointments, queues, history and theme preference remain present.

## Compact Home

- [ ] App background fills the status-bar area without a mismatched strip or unsafe overlap.
- [ ] Home content begins safely below status icons and uses noticeably less empty space.
- [ ] A hamburger icon appears at top left and opens More.
- [ ] Doctor/Assistant name and details are compact, readable and remain on one line where possible.
- [ ] A white circular profile-image placeholder appears beside the identity and can show a face-sized future image.
- [ ] Notification remains available at the top and its unread badge still works.
- [ ] Day/Night and Logout are absent from Home and available inside More.
- [ ] Today uses a smaller heading.
- [ ] Morning and Evening cards show total appointment counts for their respective sessions, centered in equal cards.
- [ ] Live queue has a top-right View action that opens Queue Control.
- [ ] The duplicate Today's appointments card is absent.

## Bottom navigation

- [ ] Only Home, Appointments and Clinic are shown, in that order.
- [ ] Home is selected on Home; Appointments and Clinic select correctly on their pages.
- [ ] More is accessible from the top-left hamburger, not the bottom bar.
- [ ] Assistant Clinic access remains permission-controlled.
- [ ] Navigation remains above system buttons on different phone sizes.

## Queue controls

- [ ] Queue Control shows Start instead of Start queue.
- [ ] When no waiting Patient remains, the action shows Complete instead of Complete consultation.
- [ ] All queue actions retain their prior behavior.

## Monotonic late-token scenario

1. Progress the Morning queue until token 15 is the Last token.
2. Admit a previously late lower token such as token 6 at its adjusted queue position.
3. Call token 6 into consultation.

- [ ] The queue list identifies token 6 as In consultation.
- [ ] The Last token card remains 15 and never changes backward to 6.
- [ ] Patient queue tracking also keeps the progress marker at 15.
- [ ] Completing token 6 leaves Last token at 15.
- [ ] Calling a later token 16 advances Last token to 16 normally.
- [ ] Restarting the App preserves the monotonic progress marker and active/completed status.

## Regression

- [ ] Morning and Evening queues remain independent.
- [ ] Fee confirmation, compulsory receipt generation, walk-in intake, skip/rejoin and archive behavior pass.
- [ ] Doctor-only actions remain unavailable to Assistants without permission.
- [ ] Notifications, reports, availability, announcements, backup and hosted workspace remain functional.

Record failures with phone model, Android version, role, session, theme, tokens and screenshot.