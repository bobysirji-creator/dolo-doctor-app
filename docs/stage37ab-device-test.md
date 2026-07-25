# Stages 37A-37B device checklist

Prerequisites: Platform API `0.28.0-stage37ab` must pass GitHub Actions and deploy live before Doctor App `0.23.0-stage37b` (version code 36) is built and installed over the current stable Doctor App.

## Hosted API

- [x] `/health` reports `0.28.0-stage37ab`; `/ready` reports ready with no blockers.
- [x] Capabilities report stage `37.2`, transport `DOCTOR_TARGETED_IN_APP_CAMPAIGN_FEED`, and targeted delivery `PATIENT_AND_DOCTOR_IN_APP_FEEDS`.
- [x] SMS remains OTP-only and disabled; Push, Maps and Payments remain disabled.

## Doctor-targeted campaign

- [x] In Admin, search Doctors, save a Doctor audience containing Dr. Ananya Mehta, create a currently active informational, promotional or app-update campaign, and approve it.
- [x] Log in to Doctor App as Doctor, connect the hosted Doctor with demo PIN `1234`, and refresh.
- [x] The campaign appears under `Targeted DO-LO messages` with its type, date period and `In-app only` label.
- [x] A Patient-targeted campaign never appears in the Doctor feed.
- [x] Cancelling the Doctor campaign in Admin removes it after Doctor refresh.
- [x] A future campaign remains hidden before its start date; an expired campaign remains hidden after its end date.

## Role and safety boundaries

- [x] Disconnect the hosted Doctor, log in locally as Assistant and connect the hosted Assistant. No targeted Doctor message section or Doctor campaign appears.
- [x] Direct Assistant access to `/api/v1/staff/campaigns` is denied with HTTP 403.
- [x] Closing/reopening the hosted workspace and completely restarting the Doctor App reloads the authoritative Doctor feed.
- [x] While offline, refresh reports an error without changing local profile, clinics, assistants, queues, appointments, history, reports, announcements, credentials or backups.
- [x] Reconnection restores refresh without duplicate campaign cards.
- [x] Existing hosted queue, fee admission, schedule, profile review, announcements, reviews, notifications and Assistant permission workflows still work.
- [x] No promotional SMS, Android Push, Maps or Payment provider activity occurs.

Record GitHub Actions links, Render deployment, stable APK upgrade and observations before accepting Stage 37.