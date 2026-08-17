# Stage 63P-B device checklist

Use only the separate controlled-pilot Supabase/Render environment. Do not change the current prototype environment.

## Deployment and build

- [ ] Platform API GitHub Actions passes, including migration 060 and PostgreSQL runtime checks.
- [ ] Pilot Render deploys `0.59.0-stage63pb`; `/ready` is ready and capabilities show Stage 63.2 with `CONTROLLED_PILOT_ANDROID_IDENTITY`.
- [ ] Each Android repository has repository variable `DOLO_API_BASE_URL` set to the pilot Render HTTPS origin.
- [ ] Patient, Doctor, and Admin GitHub Actions pass and produce stable APK artifacts.
- [ ] Stable APKs update the existing installations without uninstalling and preserve unrelated local data.

## Admin invitation flow

- [ ] In Admin App choose Controlled pilot and sign in with the bootstrapped Admin DO-LO ID and private credential.
- [ ] Restart the app; the encrypted Admin session restores without asking for the credential again.
- [ ] Create one Patient invitation and copy its 32-character code immediately.
- [ ] Create one Doctor invitation and copy its 32-character code immediately.
- [ ] The invitation result shows no stored plaintext credential, and the code is not available again after leaving the result.

## Patient activation and session

- [ ] On Patient login choose controlled pilot access, then Activate invite.
- [ ] Enter the Patient invitation code and create a private credential of at least eight characters.
- [ ] Activation returns a `DLO-PAT-NNNNNN` identity; no demo OTP is required and no local profile/family/favourite is uploaded.
- [ ] Fully close and reopen the app; the same Patient session and DO-LO ID restore.
- [ ] Hosted Patient data loads only for this account; local data remains separately intact.
- [ ] Sign out, then sign in again using the Patient DO-LO ID and private credential.

## Doctor activation and safe setup boundary

- [ ] On Doctor login choose Controlled pilot, then Activate invite.
- [ ] Enter the Doctor invitation code and create a private credential of at least eight characters.
- [ ] Activation returns a `DLO-DOC-NNNNNN` identity and survives a full app restart.
- [ ] Before Stage 63P-C onboarding, the app clearly shows Clinic setup required and does not expose or attach the demo Doctor clinic, appointments, or queue.
- [ ] Check setup status returns the safe setup-required message rather than a generic failure.
- [ ] Sign out, then sign in again using the Doctor DO-LO ID and private credential.

## Fail-closed checks

- [ ] A Patient invitation entered in Doctor App is rejected without consuming the invitation; it can still be activated in Patient App.
- [ ] A Doctor invitation entered in Patient App is rejected without consuming the invitation; it can still be activated in Doctor App.
- [ ] Reusing a successfully claimed invitation is rejected.
- [ ] Wrong credentials are rejected; five consecutive failures trigger the temporary server lock.
- [ ] Turning off network produces a clear error and does not replace the pilot session with a demo session or alter local records.
- [ ] Logout clears the app session and hosted cache; Android Back does not reopen an authenticated screen.

Record any failed box exactly as displayed. Do not copy invitation codes, private credentials, tokens, phone numbers, or environment secrets into issues, screenshots, logs, or chat.