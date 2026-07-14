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
- assistant deletions and the login session persist locally; other business-data changes are not persistent yet;
- no SMS, push, maps or payment providers;
- no Doctor/Admin broadcast delivery;
- no production signing.

## Next recommended stage

Stage 2: persisted local role/session, input validation and a permission-aware Assistant UI foundation before expanding editable doctor and clinic data.
## Stage 2 status

Version 0.2.0-stage2 (version code 2) replaces one-tap role selection with validated mobile-number and four-digit PIN credentials. Doctor, queue-enabled Assistant and view-only Assistant test identities are available. Successful sessions persist locally across app restarts and logout removes the stored session.

Assistant identity now selects an individual permission set. Queue visibility, appointment visibility, queue pause/resume, call-next, arrival and absence controls are permission-aware. The same permissions are checked again inside `DoctorViewModel`, so invoking an action without its required permission has no effect. Doctor-only profile, clinic, availability, announcements and staff routes remain inaccessible to Assistants.

Unit tests cover credential validation, role matching, Doctor actions, permitted Assistant actions, view-only restrictions and Doctor-only permission administration.

Next recommended stage: Stage 3 doctor profile, clinic and consultation schedule management with validated persisted edits and sensitive-field Admin-review flags.
## Stage 2 corrective release

Version 0.2.1-stage2 (version code 3) makes a restored local session automatically resume the saved Doctor or Assistant workspace after app relaunch. Session writes and logout clearing are committed synchronously so a rapid close/relaunch cannot race an asynchronous preference write.

Doctors can now delete an assistant from the local clinic workspace after an explicit confirmation. The ViewModel rejects the same operation when invoked by an Assistant. Removed assistant IDs persist locally and their credentials are rejected on later login attempts. Other assistant and business-data changes remain in-memory prototype behavior until the shared backend stage.
## Stage 2 device-lifecycle and theme release

Version 0.2.2-stage2 (version code 4) addresses physical-device reports from Vivo and Samsung phones. Authentication is reconstructed from an atomic internal session file, with SharedPreferences retained as a migration fallback. A restored session now selects the authenticated home route before the navigation graph starts, so removing the task from Android Recents does not intentionally route through login. Logout clears both persistence copies.

The light palette now matches the Patient App navy, teal, mint and neutral background colors. Cards, navigation, text and status surfaces use Material color roles instead of hard-coded white values. A moon/sun control on the dashboard enables a saved low-glare dark navy theme, including matching Android status and navigation bars.
## Stage 2 durable-session follow-up

Version 0.2.3-stage2 (version code 5) follows a second Vivo/Samsung physical-device test. Dark-mode persistence proved that `dolo_doctor_settings` survives both task finish and removal from Android Recents on those devices. The authenticated session payload is now stored primarily in that same proven preference file. The atomic file and original auth preferences remain fallback and migration sources. All three copies are cleared only by the explicit repository logout operation.

The Home logout icon now opens a confirmation dialog. Android Back and task removal do not call logout or clear any session store.