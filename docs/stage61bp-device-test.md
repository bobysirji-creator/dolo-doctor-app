# Stage 61B-P combined device checklist

Use seeded dummy identities only. Platform API `0.57.0-stage61bp` must be live and ready. Install Patient `0.45.0-stage61bp`, Doctor `0.26.0-stage61bp`, and Admin `0.20.0-stage61bp` over the accepted apps without uninstalling.

## Hosted contract

- [ ] `/health` reports `0.57.0-stage61bp`; `/ready` reports `ready`.
- [ ] Capabilities report stage `61.2`, transport `AUTHORITATIVE_CACHE_RETRY_CONFLICT_REHEARSAL`, and production traffic remains disabled.
- [ ] The cutover endpoint reports encrypted role-scoped cache, one idempotent retry, explicit HTTP 409 recovery, 24-hour maximum retention, logout purge, and forbidden local prototype upload.

## Live and cached reads

- [ ] While online, refresh the hosted workspace in each app. Each screen states that live server data is authoritative.
- [ ] Leave the apps signed in, disable network, then refresh/reopen each already-loaded hosted workspace. Previously loaded hosted reads remain visible and are labelled encrypted cached data.
- [ ] Confirm local profile, family, favourites, Doctor local clinic/queue/history, and theme settings remain intact and are not described as uploaded.
- [ ] Re-enable network and refresh. Each app returns to the live-server label and reflects the latest shared hosted state.

## Commands and conflicts

- [ ] While offline, attempt one hosted mutation. It fails without claiming success; cached data remains read-only and no local demo record is promoted to hosted state.
- [ ] On a slow/interrupted connection, submit one existing idempotent command. After recovery, verify no duplicate appointment, queue event, announcement, campaign, invoice or support item was created.
- [ ] Produce a stale-state conflict using two devices where practical: change a hosted object on device A, then act on the older copy on device B. Device B must require refresh and must not overwrite the server.
- [ ] Verify a non-idempotent command is not automatically retried.

## Session isolation

- [ ] In the Doctor App, load the Doctor workspace, disconnect, then sign in as Assistant. Doctor-only cached profile, announcement and Assistant-management data must not appear.
- [ ] Logout from each app. The hosted read cache is purged; it must not be displayed to a later session while offline.
- [ ] Restart all apps after the online recovery. Hosted session behavior and accepted local-data persistence remain unchanged.

Pass only when all three apps distinguish live/cached authority, preserve server conflicts, avoid duplicate commands, isolate roles and never upload local prototype data.