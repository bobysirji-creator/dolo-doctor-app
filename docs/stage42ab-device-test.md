# Stages 42A-42B ecosystem public identity checklist

Prerequisites: API `0.33.0-stage42ab`, Admin App `0.16.0-stage42a` (version code 17), and Doctor App `0.24.0-stage42b` (version code 37).

## Hosted API and Admin directory

- [ ] GitHub Actions is green and Render reports deployed/live.
- [ ] `/health` reports `0.33.0-stage42ab`; `/ready` has no blockers.
- [ ] Capabilities report stage `42.2`, transport `ECOSYSTEM_PUBLIC_DOLO_IDENTITY`, Admin directory identity `PUBLIC_DOLO_ID_INTERNAL_UUID_HIDDEN`, and staff identity `SELF_ONLY_ROLE_SCOPED`.
- [ ] Admin Patient results show `DLO-PAT-NNNNNN`; Doctor results show `DLO-DOC-NNNNNN`.
- [ ] Search by a displayed public DO-LO ID returns the matching account.
- [ ] Directory JSON contains no `userId`, internal account UUID or phone.
- [ ] Saving an audience from the same filters succeeds and preserves the correct recipient count.

## Admin App

- [ ] Install the stable APK over the existing app; session and local state remain safe.
- [ ] Patient and Doctor cards display public DO-LO IDs rather than UUIDs.
- [ ] Search by public DO-LO ID works for both directories.
- [ ] Existing filters, audience snapshots, campaigns, governance, finance and control still work.

## Doctor and Assistant App

- [ ] Install the stable APK over the existing app; local clinic/queue data remains safe.
- [ ] Hosted Doctor login shows a stable `DLO-DOC-NNNNNN` self identity and Doctor name.
- [ ] Hosted Assistant login clears the Doctor hosted role and shows a distinct `DLO-AST-NNNNNN` self identity.
- [ ] Assistant still receives only permitted actions and no Doctor-only profile/announcement/assistant controls.
- [ ] Refresh and complete close/relaunch restore the same role-scoped public identity.
- [ ] Offline failure is recoverable and does not alter local Doctor/Assistant data.

Production enrollment and public-ID issuance remain reserved. SMS is OTP-only and disabled; Push, Maps, Payments and media storage remain disabled.