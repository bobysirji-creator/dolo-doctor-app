# Stage 16D Physical-Device Test

**Accepted:** 2026-07-20. GitHub Actions passed and all twelve physical-device checks below passed successfully.

Use dummy data only after Platform API `0.4.0-stage16d` is deployed and ready.

1. Install stable Doctor App `0.15.0-stage16d` over the existing app. Confirm local login, clinic, queue, assistants, history and theme remain unchanged.
2. Login locally as Doctor (`9999999999`, using the current local PIN; use `1234` only if unchanged), open **Hosted Stage 16D queue**, enter PIN `1234`, and connect.
3. Confirm identity `DOCTOR`, Dr. Ananya Mehta, DO-LO Prototype Clinic, sessions, and the Patient App's previously booked authoritative token appear.
4. Tap **Confirm clinic fee and admit**. Confirm fee becomes PAID, a receipt value appears, and the appointment enters the authoritative queue. This records clinic-direct payment only; DO-LO processes no Doctor fee.
5. Start the selected queue if needed, then tap **Call next**. Confirm the correct token becomes IN_CONSULTATION.
6. Open the Patient App hosted screen and refresh. Confirm current token, patients ahead and estimate reflect the Doctor action.
7. Complete the consultation in Doctor App, refresh Patient App, and confirm COMPLETED status.
8. Close/relaunch Doctor App and reopen hosted queue. Confirm hosted session and server queue restore while online.
9. Keep the hosted screen open for at least 20 seconds. Confirm refresh does not duplicate admissions or commands.
10. Disconnect hosted session. Confirm the local Doctor login and all local clinic data remain unchanged.
11. Login locally as queue-enabled Assistant (`9876543210`, using its current local PIN; use `1234` only if unchanged), open hosted queue, connect with PIN `1234`, and confirm MANAGE_QUEUE plus CONFIRM_CLINIC_FEE only. Verify queue actions work.
12. Disable connectivity and refresh. Confirm an error appears without crash or local-data loss; reconnect and confirm recovery.

Do not use real staff or patient information. Do not share tokens. SMS, Push, Maps and Payments remain disabled.