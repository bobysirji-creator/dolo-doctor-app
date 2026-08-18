# Stage 63P-C hosted and device checklist

Use the separate controlled-pilot Render URL and pilot Supabase database. Do not run these checks against the older seeded prototype service.

## Deployment

- [ ] Platform API GitHub Actions is green, including PostgreSQL migrations/integration.
- [ ] Pilot Render deploy is live at version `0.60.0-stage63pc`; `/ready` reports `ready` and capability stage is `63.3`.
- [ ] Doctor App and Admin App GitHub Actions are green and their stable APKs update the existing apps.

## Doctor submission and rejection

- [ ] Sign in to the already activated pilot Doctor account. The first-clinic setup form appears; no demo clinic or demo queue appears.
- [ ] Enter genuine test Doctor/clinic details, select working days, verify Morning/Evening timings, and submit.
- [ ] The Doctor screen changes to `PENDING`; fields cannot be edited while Admin review is pending.
- [ ] In Admin App > Reviews, the Doctor DO-LO ID, registration/profile, clinic address/contact, clinic fee, booking policy and weekly sessions all match.
- [ ] First reject with a clear test note. The Doctor sees the note after refresh and can edit/resubmit the saved values.

## Approval and live workspace

- [ ] Admin approves the resubmission with `Approve and create clinic`.
- [ ] Doctor refreshes setup status, then opens the hosted clinic. The approved clinic, sessions, empty appointments and empty queue load without seeded data.
- [ ] Patient App hosted discovery shows the new verified clinic and its available sessions.
- [ ] Book one current/future permitted test appointment in Patient App; Doctor App receives it in the matching session.
- [ ] Confirm clinic fee, admit, run queue actions and verify Patient live-queue refresh. Clinic fee is direct-to-clinic; no real online payment is moved.

## Safety and persistence

- [ ] Closing/relaunching Doctor and Admin apps restores the pilot sessions and approved workspace.
- [ ] A second approval/replayed command does not create a duplicate clinic or schedule.
- [ ] A Patient/Assistant cannot call Doctor onboarding, and a Doctor cannot call Admin review.
- [ ] Logout clears only hosted session/cache; existing local prototype data remains separate and unchanged.

Record any failed box and its exact on-screen/API message before continuing to Stage 63P-D.