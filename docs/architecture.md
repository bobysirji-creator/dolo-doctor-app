# Architecture

## Current Stage 1

- Kotlin and Jetpack Compose single-module Android app
- Material 3 mobile-first UI
- `DoctorViewModel` for local prototype state
- immutable models and deterministic dummy data
- role-aware navigation for Doctor and Assistant
- provider-neutral service interfaces in `integrations/`
- GitHub Actions as the build environment

## Future boundaries

The Android app must never embed production SMS, payment, map, push or privileged backend credentials. A shared HTTPS backend will own authentication, authorization, doctor verification, appointment/token allocation, queue state, announcements, availability, broadcasts and audit logs.

## Planned backend resources

- `/auth/doctor/login`, `/auth/assistant/login`
- `/doctor/profile`, `/doctor/clinics`, `/doctor/schedules`
- `/doctor/queue`, `/doctor/appointments`
- `/doctor/availability-blocks`
- `/doctor/announcements`
- `/doctor/assistants`, `/doctor/assistants/{id}/permissions`
- `/admin/broadcasts`, `/admin/doctor-verification`

Every assistant action must be authorized on the server, not only hidden in Compose navigation.