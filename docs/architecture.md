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
## Stage 3 local queue lifecycle

- DoctorUiState.queueDate identifies the active clinic day using ISO yyyy-MM-dd.
- DailyQueueHistory is an immutable local snapshot of the closed date, final token and complete appointment list.
- QueueStateCodec uses URL-safe Base64 field encoding before storing current appointments and history records in SharedPreferences.
- Manual close is Doctor-only. A closed date rejects queue and appointment mutations.
- A later device date archives the previous queue once and initializes an empty NOT_STARTED queue.
- The shared backend will eventually replace device time and local storage as the authoritative clinic timezone, queue session and audit source.