# DO-LO Doctor App Roadmap

- [x] Stage 1 - lightweight Compose project, Doctor/Assistant login, navigation and feature skeletons
- [x] Stage 2 - process-safe authentication and workflow state, Patient-aligned light/dark themes, permission-aware assistant access and Doctor-only removal
- [x] Stage 3 - dated queue lifecycle, Doctor-only close-day archival, automatic next-day reset and persisted appointment history
- [x] Stage 4 - validated, persisted doctor profile, clinic and consultation schedule management
- [x] Stage 5 - expanded appointment workflow with queue audit events
  - [x] Stage 5.2 - independent Morning/Evening queues and automatic session booking cutoffs
  - [x] Stage 5.3 - fee-confirmed queue admission, independent session tokens and 58 mm receipt layout
  - [x] Stage 5.4 - independent session closure, enforced token capacity and persistent in-app notifications
- [x] Stage 6 - appointment availability blocks and affected-patient workflow
  - [x] persisted date-range and Morning/Evening/Both booking controls
  - [x] affected-patient follow-up states and queue safety enforcement
  - [x] availability audit/notification events and backend-ready contracts
- [x] Stage 7 - announcements, camps, offers and Patient App profile feed contracts
  - [x] validated create, edit, publish, draft and delete workflow
  - [x] persisted scheduling with live/expired Patient profile visibility
  - [x] audit notifications and provider-neutral cross-app feed contracts
- [x] Stage 8 - assistant creation, credentials and backend-ready permission administration
  - [x] persisted Doctor-only assistant creation, enable/disable and deletion
  - [x] generated temporary PINs stored only as salted hashes with reset support
  - [x] modular permission administration, audit events and backend service contracts
- [x] Stage 9 - reports, feedback, multi-clinic readiness and queue-delay notices
  - [x] Assistant Clinic-access correction with read-only permission enforcement
  - [x] persisted operational metrics, feedback acknowledgement and delay notices
  - [x] clinic-ID-scoped reporting and backend integration contracts
- [ ] Stage 10 - shared backend integration with Patient App
- [ ] Stage 11 - accessibility, security, tests and release hardening

Admin App remains a separate future repository. Real providers remain disabled until the shared backend and policies are approved.
