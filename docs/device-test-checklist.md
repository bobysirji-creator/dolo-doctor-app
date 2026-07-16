## Stage 9 Clinic permission, reports, feedback and delay notices

1. Login as Doctor, open Assistants and grant **View clinic** to a test Assistant. The older **Manage clinic availability** permission must also provide migrated Clinic viewing access.
2. Login as that Assistant. Confirm the Clinic card is active, opens Clinic & schedule and displays contact details, both sessions, token capacity and average consultation time.
3. Confirm no **Edit clinic & schedule** control appears for the Assistant. Direct workflow tests must continue rejecting Assistant clinic mutations.
4. Remove both Clinic-related permissions and login again. Confirm the Clinic card is disabled and navigation cannot open the page.
5. As Doctor, open Reports. Confirm appointment, completed, absent, fee-pending and confirmed-collection metrics match current plus archived local data.
6. Confirm the feedback summary shows average rating and count. Acknowledge one feedback item and verify its badge changes to Acknowledged.
7. Remove the app from Recents and relaunch. Confirm the acknowledgement persists and appears in Activity log and Notifications.
8. Grant only **View reports** to an Assistant. Confirm summary reports are visible but detailed feedback and delay creation remain unavailable.
9. Grant **View patient feedback** and verify detailed feedback plus acknowledgement become available without granting Doctor-only routes.
10. Grant **Send queue delay notice**, create a 25-minute Evening notice and verify session, delay, message, creator and time appear in Reports.
11. Test delays below 5 or above 240 minutes and a message shorter than 5 characters; each must stay in the dialog with validation feedback.
12. Relaunch and verify delay notices persist. Confirm Activity log and Notifications contain the queue-delay event.
13. Review Multi-clinic readiness in Reports and confirm every configured clinic record is listed. No independent clinic switch is expected until Stage 10 backend synchronization.
## Stage 8 assistant accounts and generated credentials

1. Login as Doctor, open Assistants and choose **Add assistant**.
2. Enter a valid new name and unused 10-digit mobile number, select a small permission set and create the account.
3. Record the generated temporary PIN before pressing **I saved it**; confirm the PIN cannot be reopened from the screen.
4. Logout and login as the new Assistant using the generated mobile/PIN. Confirm only the selected dashboard tools and actions are available.
5. Logout, return as Doctor and grant one additional permission. Login as the Assistant again and confirm the new capability appears while Doctor-only profile, staff and ownership routes remain unavailable.
6. Disable the assistant account. Confirm its credentials are rejected and a previously saved session does not restore after relaunch.
7. Re-enable the account and confirm the same PIN works again.
8. Use **Reset PIN**, save the new PIN, then confirm the old PIN fails and the new PIN succeeds.
9. Remove the app from Recents and relaunch. Confirm assistant profile, enabled state and permissions persist.
10. Delete the new assistant after confirming the warning. Confirm its mobile/PIN can no longer login after relaunch.
11. Test invalid short names, invalid mobile numbers and a duplicate assistant mobile; each must remain in the form with a clear validation message.
12. Open Activity log and Notifications as Doctor. Confirm create, status, permission, PIN-reset and delete events are present and affect the unread badge.
## Stage 7 doctor updates and Patient profile feed

1. Login as Doctor, open Updates, and verify the Live and Scheduled counters are visible.
2. Create a General update as a draft using today's ISO date, a valid title and a message of at least 10 characters.
3. Verify the saved record shows Draft and does not appear under Active doctor updates on Home.
4. Publish it and verify it changes to Live and appears on Home.
5. Create a future Camp with Published enabled. Verify it shows Scheduled, is retained in Updates, and does not appear on Home yet.
6. Create an Offer whose end date is before today. Verify it shows Expired and is excluded from Home.
7. Edit the live update's title, message, type and date range; verify the changes appear immediately without creating a duplicate.
8. Try a title under five characters, a message under ten characters, an invalid date and an end date before the start. Verify each is rejected without closing the form.
9. Move the live record back to Draft and verify it disappears from Home but remains editable.
10. Delete a test record, dismiss the confirmation once, then confirm; verify only the selected record is removed.
11. Open Notifications and verify save, publish/draft and delete events appear with unread-badge behavior.
12. Remove the app from Recents, relaunch, and verify every remaining update, date, type and publication choice persists.

## Stage 5.4 session closure, capacity and notifications

1. Open Live queue, select Morning, tap **Close Morning session**, dismiss once, then confirm.
2. Verify only Morning becomes CLOSED. Evening must retain its previous state, remain selectable, and accept a walk-in if its cutoff and capacity allow it.
3. Verify Queue history is not finalized yet. Close Evening and then confirm one complete dated history record appears.
4. If a patient is IN CONSULTATION when its session is closed, verify that patient becomes COMPLETED while the other session's patients remain unchanged.
5. Open Clinic, set Maximum tokens per session equal to the current Morning appointment count, save, and return to Today's appointments.
6. Verify Morning displays used/max with LIMIT REACHED, its walk-in choice is disabled, and a workflow submission cannot create another Morning token. Verify Evening follows its own count against the same configured maximum.
7. Raise the maximum by one, save, and verify exactly one additional Morning walk-in succeeds before the limit is reached again.
8. Perform a queue action or book a walk-in, return Home, and verify the notification bell shows an unread badge.
9. Open Notifications and verify the newest event shows its action, detail, actor, date/time and token/patient when applicable.
10. Tap **Mark all read**, return Home, and verify the badge clears. Remove the app from Recents, relaunch, and verify the cleared read state persists.
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

## Stage 5.1 skipped/late patients, walk-ins and receipts

1. While a patient is IN_CONSULTATION, press Skip. Before calling another patient, press Resume consultation now and confirm the same token returns to IN_CONSULTATION.
2. Skip again, press Rejoin at end, then repeatedly Call next. Confirm normal waiting tokens are handled first and the skipped token eventually returns to IN_CONSULTATION without receiving a new token.
3. After the queue has progressed, mark an earlier BOOKED token as Late arrival/rejoin. Confirm the original token remains unchanged and is called after the patients already ahead in queue order.
4. Confirm Resume consultation now is hidden while another patient is IN_CONSULTATION, preventing two active consultations.
5. Login as Doctor or queue-enabled Assistant, open Today's appointments and choose Book walk-in patient. Enter valid details and confirm the next token is allotted, status is ARRIVED and source is clinic walk-in.
6. Confirm the receipt opens automatically with a large bold token, patient/doctor/clinic/date/session and stable receipt number.
7. Press Print receipt. Confirm the Android print screen opens; test Save as PDF or a printer exposed through an installed Android print service.
8. Close the receipt, open it again from the appointment and confirm token/receipt number do not change and no duplicate appointment is created.
9. Mark a future online booking Arrived and confirm its receipt is generated and available from Today's appointments.
10. Login as the view-only Assistant and confirm walk-in booking and receipt actions are unavailable.
11. Remove the app from Recents and relaunch. Confirm walk-in appointment, queue order, booking source and receipt number persist.

## Stage 5.2 Morning and Evening session queues

1. Open Live queue and confirm Morning and Evening selectors show independent status, current token and appointment lists.
2. Start or pause Morning, switch to Evening, and confirm the Evening state and token did not change. Repeat in the opposite direction.
3. Book an Evening walk-in before the displayed Evening start time. Confirm it succeeds, appears only in Evening, receives the next unique daily token and starts at the end of the Evening order.
4. Call next in Evening and confirm no Morning appointment status or current token changes. Then call next in Morning and confirm Evening remains unchanged.
5. In Clinic, temporarily set the Morning end time a few minutes ahead, save, wait until that time, and return to Today's appointments. Within one minute Morning must show booking closed while Evening remains bookable.
6. Confirm the walk-in dialog disables the closed Morning choice and a direct Morning booking attempt is also rejected by the workflow layer.
7. Temporarily set the Evening end time a few minutes ahead and confirm Evening also closes at that time. Existing queue controls must remain usable so already-booked consultations can finish.
8. Restore the intended clinic schedule after cutoff testing.
9. Close the app, remove it from Recents and relaunch. Confirm both session states/current tokens and their appointment membership persist.
10. Next-day reset/reopening is covered by unit tests; do not change the phone date solely for testing unless comfortable restoring automatic date/time afterward.
11. Thermal receipt printing is intentionally deferred until the target printer is available; confirm only that receipt generation/reopen still works in this build.

## Stage 5.3 fee desk, independent tokens and 58 mm receipt

1. Open Today's appointments. Confirm unpaid online bookings show FEE PENDING and do not appear in Live queue.
2. For a pending Morning appointment, select Confirm fee & admit to queue, verify the default doctor fee, choose Cash or UPI and confirm. Verify the receipt opens and the appointment now appears in Morning Queue as ARRIVED.
3. Confirm the receipt and appointment show the same fee amount, payment method, payment time, session and token. Close/reopen the receipt and verify these values do not change.
4. Test Waived on a suitable demo appointment and verify the receipt clearly says CONSULTATION FEE: WAIVED with amount zero.
5. Login as the view-only Assistant and verify fee confirmation is unavailable. Login as Neha Kapoor and verify fee confirmation and walk-in fee collection are available.
6. Book one new Morning walk-in and one new Evening walk-in. Verify each receives the next number only from its own session; Evening should not continue Morning's token count.
7. Switch Appointments to Evening, return Home and reopen Appointments; Evening must remain selected. Repeat by moving between Queue, Home and Appointments, then remove the app from Recents and relaunch.
8. In Live queue, confirm only receipt-issued paid/waived appointments are present. Call next and ensure no fee-pending appointment can be called.
9. Print a receipt through the installed ESC/POS 58 mm print service. Verify DO-LO, clinic, session/token, patient, fee and receipt number are horizontally centered rather than aligned to the left.
10. Verify the 58 mm print is not clipped, the token is large, paper feed is reasonable and no content crosses the printable width. Report the printer model/app and a photo if centering or scaling still needs calibration.
11. Remove the app from Recents and relaunch. Confirm payment status, fee, receipt, independent token values and selected session all persist.

## Stage 6 appointment availability

1. On Home, verify theme, notification and logout icons are in a separate upper row and the complete Doctor name stays on one line where the screen width permits.
2. Open Availability and add a Morning-only block covering today's ISO date with Bookings disabled and a reason of at least five characters.
3. Verify Morning booking closes while Evening stays available. Confirm a Morning clinic walk-in and pending-fee admission are rejected with the saved reason.
4. Verify existing non-terminal Morning appointments appear under the block with CONTACT PENDING and remain visible in Live queue.
5. In Live queue, verify an affected appointment cannot be marked arrived or called while follow-up is unresolved.
6. In Availability, mark one patient Notified, another Needs reschedule and one Resolved. Verify each status updates immediately.
7. Return to Live queue and verify only the Resolved affected patient can proceed when eligible.
8. Re-enable bookings on the block and verify affected flags clear and Morning booking becomes available again, subject to its cutoff, capacity and queue state.
9. Edit the block to Evening or Both, then close/reopen the app and verify its dates, session, reason and enabled state persist.
10. Delete the block and verify bookings reopen unless another active block covers the same date/session.
11. Open Notifications and verify availability save/change/delete and affected-patient follow-up events appear and participate in the unread badge.
