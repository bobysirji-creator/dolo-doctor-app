# Stages 25C-25D Doctor device checks

Use the complete cross-app checklist in docs/stage25cd-device-test.md in the Patient or Admin repository. Doctor-specific acceptance:

- [x] Stable Doctor APK updates the existing installation and preserves local clinic, queue, credentials, settings and backups.
- [x] A Doctor hosted login shows only Admin-PUBLISHED reviews for the owned clinic.
- [x] Each published review is read-only and has the correct rating, Patient, clinic, comment and date.
- [x] PENDING, HIDDEN and REJECTED reviews never appear.
- [x] An Assistant hosted login has no Patient review feed or Doctor-only review action.
- [x] Refresh, restart, offline failure and reconnection retain safe behavior.
- [x] Queue, fee admission, receipts, schedule, announcements and Assistant controls remain operational.
- [x] Maps, Payments, SMS and Push remain disabled.
