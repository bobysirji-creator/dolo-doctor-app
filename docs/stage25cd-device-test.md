# Stages 25C-25D Doctor device checks

Use the complete cross-app checklist in docs/stage25cd-device-test.md in the Patient or Admin repository. Doctor-specific acceptance:

- [ ] Stable Doctor APK updates the existing installation and preserves local clinic, queue, credentials, settings and backups.
- [ ] A Doctor hosted login shows only Admin-PUBLISHED reviews for the owned clinic.
- [ ] Each published review is read-only and has the correct rating, Patient, clinic, comment and date.
- [ ] PENDING, HIDDEN and REJECTED reviews never appear.
- [ ] An Assistant hosted login has no Patient review feed or Doctor-only review action.
- [ ] Refresh, restart, offline failure and reconnection retain safe behavior.
- [ ] Queue, fee admission, receipts, schedule, announcements and Assistant controls remain operational.
- [ ] Maps, Payments, SMS and Push remain disabled.
