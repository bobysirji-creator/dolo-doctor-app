# Stage 20B physical-device checklist

Prerequisites: API `0.11.0-stage20a` is live; Doctor and Patient stable APKs are installed; the seeded Doctor is active and VERIFIED.

- [ ] Stable Doctor APK updates the existing app without uninstalling; local login, clinic, queue and backup data remain intact.
- [ ] Hosted Doctor login with demo PIN `1234` loads the schedule editor.
- [ ] Set future booking to `0`, save, refresh Patient Hosted Prototype Sync, and confirm only the current date is offered.
- [ ] Restore a future window such as `2`, then confirm the eligible future dates return.
- [ ] Disable one weekday Morning session and confirm that session is not bookable while Evening remains governed independently.
- [ ] Close one future Morning session with a date exception and confirm Evening remains available; reopen it and confirm normal weekly availability returns.
- [ ] Change max tokens and average consultation minutes, save, leave/reopen the screen, and confirm values persist from the server.
- [ ] Confirm existing appointments/history are retained after a schedule or exception change.
- [ ] Fully close/relaunch both apps; hosted sessions restore and the saved schedule reloads.
- [ ] While offline, a schedule save fails visibly and no local Doctor/Patient data changes; reconnect and refresh successfully.
- [ ] Hosted Assistant login exposes no schedule, profile, announcement or Assistant-management editor.
- [ ] Maps, Payments, SMS and Push remain disabled.

After all checks pass, restore the intended prototype weekly schedule/future-booking window and record acceptance in roadmap and handoff documentation.
