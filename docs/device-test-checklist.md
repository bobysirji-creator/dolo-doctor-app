# Stage 2.2 Physical Device Checklist

Run on both Vivo and Samsung devices.

## Session lifecycle

1. Login as Doctor.
2. Open Queue, press Call next, then use Android Back until the app closes.
3. Note the new current token and appointment statuses, then relaunch from the launcher; Doctor Home must open without login and show the same token/status values.
4. Remove DO-LO Doctor from Android Recents.
5. Relaunch; Doctor Home must still open without login and the queue must remain at the same saved state.
6. Repeat steps 1-5 with each Assistant account.
7. Tap Logout, dismiss the confirmation, remove the app from Recents, and relaunch; the session must remain.
8. Tap Logout again, explicitly confirm, remove the app from Recents, and relaunch; login must be required.

## Theme

1. Confirm light mode visually matches the Patient App navy/teal palette.
2. Tap the moon icon on Home and confirm all pages, cards and bottom navigation become dark.
3. Close and remove the app from Recents, relaunch, and confirm dark mode remains selected.
4. Tap the sun icon to return to light mode.

## Assistant deletion

1. As Doctor, delete one Assistant and confirm the dialog.
2. Remove the app from Recents and relaunch.
3. Confirm the Assistant remains removed and their credentials are rejected.
## Stage 3 daily queue lifecycle

1. Login as Doctor and note the displayed queue date, current token and patient statuses.
2. Press **Call next**, then open **Close and archive day** and dismiss the confirmation; the queue must remain active.
3. Open it again and confirm. Queue status must become CLOSED; Pause/Resume, Call next and appointment updates must no longer change data.
4. Return Home, open **Queue history**, and confirm the date, final token, appointment count, patient names and final statuses match the closed queue.
5. Close the app, remove it from Recents and relaunch. The closed queue and history entry must remain.
6. Logout and login again. The archived history must remain unchanged.
7. Login as either Assistant and confirm no Close-day control or Queue-history route is available.
8. Automatic next-date rollover is covered by unit tests for this prototype; do not change the phone date solely for testing unless comfortable restoring automatic date/time afterward.
## Stage 4 final patient, profile and clinic editing

1. Advance the queue until token 14 is IN_CONSULTATION. Press **Complete consultation** and confirm token 14 becomes COMPLETED.
2. Close and archive the day, open Queue history and confirm token 14 is archived as COMPLETED.
3. Open Profile, edit only name, fee, experience or About, save, and confirm the profile remains verified.
4. Change specialty, qualification or registration number and confirm **Sensitive changes pending Admin review** appears.
5. Enter invalid profile values and confirm the form stays open with a validation message.
6. Open Clinic, edit contact details, morning and evening sessions, token capacity and average consultation minutes, then save.
7. Close the app, remove it from Recents and relaunch. Confirm profile and clinic or schedule changes remain.
8. Login as an Assistant and confirm Profile and Clinic editing routes remain unavailable.
## Stage 5 appointment transitions and activity log

1. Login as Doctor, open **Activity log**, and note its current event count.
2. Return to Live queue. Pause and resume the queue, then confirm both actions appear at the top of Activity log with the Doctor name, date and time.
3. Mark a BOOKED patient Arrived, then Waiting. Confirm both legal changes persist and their before/after statuses appear in Activity log.
4. Confirm a WAITING patient does not show another Waiting action and a COMPLETED or ABSENT patient has no reopening controls.
5. For the IN_CONSULTATION patient, press **Complete consultation** and confirm status becomes COMPLETED with a consultation-completed audit event.
6. Login as the queue-enabled Assistant, call the next eligible patient, and confirm Activity log later attributes that call to **Neha Kapoor** rather than the Doctor.
7. Login as the view-only Assistant and confirm queue mutation controls remain disabled; the event count must not change from attempted navigation.
8. Close the app, remove it from Recents and relaunch. Confirm appointment statuses and Activity log entries remain intact.
9. As Doctor, close and archive the day. Confirm the final Activity log entry records the day close and Queue history preserves every final status.
