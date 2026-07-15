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