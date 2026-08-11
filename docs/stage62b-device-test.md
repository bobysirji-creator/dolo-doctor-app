# Stage 62B Doctor device checklist

Install the stable `dolo-doctor-stable-debug.apk` over the accepted Doctor App without clearing data.

## Upgrade and persistence

- [ ] The existing app updates successfully and retains the Doctor login, clinic setup, queue state, history and dark-theme preference.
- [ ] Closing, removing from Recents and reopening restores the same signed-in role and operational state.

## Doctor navigation

- [ ] The bottom navigation shows exactly **Today**, **Appointments**, **Clinic**, and **More**.
- [ ] Today shows morning/evening token summaries, live queue control, today's appointment intake and the hosted workspace.
- [ ] Queue Control opens from Today and the Today destination remains selected while working with the queue.
- [ ] Appointments opens directly and existing online/offline booking, clinic-fee confirmation, receipt generation and session selection still work.
- [ ] Clinic opens directly, preserves all clinic/schedule/weekly-off/future-booking controls and remains selected.
- [ ] More groups actions under Account, Clinic management, and Insights and data without the previous long dashboard grid.
- [ ] Doctor Profile, Availability, Announcements, Assistants, Reports, Queue History, Activity Log, Hosted workspace, Local Sync, Backup, PIN and Notifications all open from More.
- [ ] Back navigation returns to the expected previous screen without losing selected session or queue state.

## Assistant boundaries

Test at least one full-access demo Assistant and one restricted Assistant.

- [ ] Every Assistant sees Today, Appointments, Clinic and More, but Clinic is disabled when neither clinic permission is granted.
- [ ] More always shows Notifications and Change login PIN.
- [ ] Reports appears only with a report/feedback/delay-notice permission.
- [ ] Announcements appears only with `MANAGE_ANNOUNCEMENTS` and opens successfully when granted.
- [ ] Hosted staff workspace appears only for the seeded hosted Assistant boundary.
- [ ] Doctor Profile, Availability, Assistants, Queue History, Activity Log, Local Sync and Backup never appear for an Assistant.
- [ ] Queue and appointment buttons remain enabled/disabled by their existing individual permissions.

## Visual, accessibility and regression checks

- [ ] Light and dark themes keep all Today, More and bottom-navigation text readable.
- [ ] On a small phone and with increased Android font size, four bottom destinations remain tappable without overlap or clipping.
- [ ] Notification badge, notification list and notification tap behavior remain functional.
- [ ] Logout confirmation works from Today and More; cancellation keeps the session, confirmation clears it.
- [ ] Session-specific queues, token ordering, late-Patient handling, maximum tokens, closing/archive, reports, backup/recovery and hosted synchronization behave exactly as before.

Acceptance: every box must pass. Record any failure with role, permissions, destination, theme, font setting and screenshot before Stage 62C.