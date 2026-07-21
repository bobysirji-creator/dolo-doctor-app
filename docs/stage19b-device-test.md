# Stage 19B Doctor App device test

1. Install the new stable Doctor APK over the existing app; confirm local login, clinic and queue data remain intact.
2. Open Hosted staff queue, connect as seeded Doctor with PIN `1234`, and confirm the Reviewed Doctor profile card loads.
3. Record the approved name and specialty. Submit a clearly identifiable valid change; confirm `Pending Admin review` appears after reopening/restarting.
4. Before Admin action, confirm the Patient App still shows the old approved Doctor name/specialty.
5. In the Admin App Reviews section, confirm the pending values exactly match the Doctor submission.
6. Approve the revision with a short note. Refresh the Doctor profile and confirm the approved revision/value changes and pending status disappears.
7. Refresh/re-login the Patient App and confirm the approved name/specialty appears.
8. Submit a second Doctor revision, reject it from Admin, then confirm Doctor and Patient retain the last approved values.
9. Connect as hosted Assistant and confirm no profile editor or submission access is shown.
10. Disable internet, attempt a Doctor profile refresh/submission, and confirm the local Doctor data remains safe; reconnect and recover.

Do not enter real registration, qualification or personal data in this seeded prototype.