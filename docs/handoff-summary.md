# Doctor App Handoff

## Stage 1 status

The repository contains a lightweight Kotlin and Jetpack Compose Doctor App skeleton using the same Android baseline as the Patient App. Doctor and Assistant demo roles share one APK but receive different dashboard access.

Implemented screens: splash, role selection/login, doctor dashboard, live queue, today's appointments, clinic schedule, availability, announcements, assistants/permissions and doctor profile.

The local queue demo can pause/resume, call the next patient and update basic appointment status. Announcement visibility, availability status and assistant permissions can be toggled in local memory. These controls demonstrate the workflow only and do not communicate with the Patient App.

Backend-ready contracts reserve doctor profile, clinic, appointment, queue, announcement, availability, assistant and notification operations. GitHub Actions runs lint, unit tests and debug APK assembly and uploads an APK plus checksum.

## Important limitations

- no production login or OTP;
- no shared backend or server authorization;
- no real patient or medical data;
- no persistent changes yet;
- no SMS, push, maps or payment providers;
- no Doctor/Admin broadcast delivery;
- no production signing.

## Next recommended stage

Stage 2: persisted local role/session, input validation and a permission-aware Assistant UI foundation before expanding editable doctor and clinic data.