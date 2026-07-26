# Stages 44A-44B clinic booking-account identity checklist

Use the authoritative checklist in the Platform API repository. Verify Doctor App `0.25.0-stage44b` (version code 38) against deployed API `0.35.0-stage44ab`.

- [x] Stable APK upgrades the existing Doctor App without data loss.
- [x] SELF appointment shows `Patient DO-LO ID: DLO-PAT-NNNNNN`.
- [x] FAMILY appointment shows `Booking account: DLO-PAT-NNNNNN | Family appointment`.
- [x] The ID remains unchanged after clinic-fee confirmation and queue admission.
- [x] Doctor and Assistant see the same clinic-scoped ID; Assistant has no Doctor-only actions.
- [x] Restart/session restoration and offline/reconnection safety pass.
- [x] No Patient phone, internal account UUID or patient-profile UUID is displayed.
- [x] External providers remain disabled.