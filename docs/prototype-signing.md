# Stable prototype APK signing

## Why this is required

Android accepts an in-place update only when the installed app and replacement APK use the same application ID and signing certificate. Earlier GitHub-hosted builds used a temporary debug key created on a fresh runner, so their certificates changed and Android reported **App not installed**.

Version 0.12.1-stage12 introduces one persistent prototype certificate supplied through encrypted GitHub Actions repository secrets. It is not a production Play Store key.

## One-time GitHub setup

Open the repository's [Actions secrets page](https://github.com/bobysirji-creator/dolo-doctor-app/settings/secrets/actions). Choose **New repository secret** four times.

The private values were generated outside the repository in:

`C:\Users\Poly\Documents\codex\private\dolo-doctor-prototype-signing\individual-github-secrets`

For each file below, use the filename without `.txt` as the secret name and copy the complete file contents as its value:

1. `DOLO_SIGNING_KEYSTORE_BASE64.txt`
2. `DOLO_SIGNING_STORE_PASSWORD.txt`
3. `DOLO_SIGNING_KEY_ALIAS.txt`
4. `DOLO_SIGNING_KEY_PASSWORD.txt`

Do not paste any value into chat, an issue, source code, workflow YAML, or a commit. After all four secrets exist, push the signing-workflow commit to `main`.

## Private backup

Keep the entire private signing folder backed up in at least one secure offline location. The GitHub secrets cannot be read back after saving. Losing the PKCS#12 file or its password means future APKs cannot update installations signed with this certificate.

Never rotate this prototype key for routine builds. A replacement certificate again requires uninstalling the old app unless a future Play App Signing migration supports an authorized key upgrade.

## CI behavior

For pushes to `main` and manual workflow runs:

- all four secrets are mandatory;
- the PKCS#12 file is reconstructed only in the runner's temporary directory;
- Gradle signs both debug and release APKs with the prototype certificate;
- `apksigner` verifies the debug APK;
- the workflow compares the APK certificate digest with the restored keystore certificate;
- the artifact is named `dolo-doctor-stable-debug-apk`;
- the APK checksum and `signing-certificate.sha256` are included.

Pull-request builds receive no signing material, compile with the default temporary debug configuration, and publish no installable artifact.

## First stable installation

The APK currently installed on the phone was signed by an older temporary key and cannot be updated by the new stable key.

1. Complete any final checks or screenshots needed from the old installation.
2. Understand that uninstalling erases the current local prototype data because Android backup is disabled.
3. Uninstall the existing DO-LO Doctor app once.
4. Install the first `dolo-doctor-stable-debug-apk`.
5. For every later build, install over the existing app without uninstalling.
6. Confirm that login, clinic state, appointments and queues survive the later in-place update.

Production release signing, Play App Signing, key custody, rotation policy, and recovery remain separate future work.
