# Stages 28A-28B staff notification device checklist

Prerequisites: deploy API `0.19.0-stage28ab` and confirm API/Doctor Actions are green. Install Doctor App `0.22.0-stage28b` over the existing stable app. Keep the Patient App available to create a new authoritative online booking.

- [ ] Existing local and hosted Doctor/Assistant login, clinic, queue, appointment, schedule, profile, announcement, review and backup data remain intact.
- [ ] Confirm `/health` reports `0.19.0-stage28ab`, `/ready` is ready, and capabilities report stage `28.2`, transport `AUTHORITATIVE_IN_APP_STAFF_NOTIFICATIONS`, and `staffNotifications = CLINIC_SCOPED_SERVER_READ_CURSOR`.
- [ ] Log in locally and connect the matching seeded hosted Doctor; confirm the Home bell shows the hosted unread count.
- [ ] Open Notifications and confirm hosted cards show clear event copy, the correct Patient/family name and matching token.
- [ ] Tap one hosted card, then Mark all read; confirm the Home badge clears after synchronization.
- [ ] Fully close and relaunch; confirm read events stay read and the badge does not return without a new clinic event.
- [ ] Book one new hosted appointment from the Patient App; within about 15 seconds confirm the Doctor Home badge returns without manual restart.
- [ ] Confirm the new card says `New online appointment` and carries the newly booked Patient/family name and token.
- [ ] Sign out locally, log in as Assistant and connect the matching seeded hosted Assistant; confirm the same assigned clinic activity is visible.
- [ ] Mark the Assistant feed read, reconnect as Doctor, and confirm Doctor and Assistant read state are independent.
- [ ] Confirm no notification from an unassigned clinic is exposed and role switching still clears a mismatched hosted session.
- [ ] Disable connectivity and refresh; confirm previous hosted/local data remain safe and no event is falsely read or duplicated.
- [ ] Reconnect and confirm authoritative notifications recover without duplication.
- [ ] Confirm there is no Android system push notification and no SMS; Push, SMS, Maps and Payments remain disabled.