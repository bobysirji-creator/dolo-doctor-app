# Security Policy

## Prototype status

DO-LO Doctor is a local workflow prototype and is not approved for real patient, payment or medical data. The Stage 10/11 shared transport is process-local and the APK has no Internet permission.

## Reporting a vulnerability

Do not publish patient data, credentials, tokens or exploit details in a public issue. Use GitHub's private security-advisory reporting for this repository. Include the affected version, reproduction steps, impact and the smallest safe proof of concept.

## Production security gates

A production release requires:

- a hosted HTTPS backend and environment-specific allowlisted domains;
- short-lived access tokens, rotated refresh tokens and server-side revocation;
- server authorization for every Doctor and Assistant action;
- OTP/PIN rate limiting, lockout and recovery controls;
- encrypted server secrets and no provider credentials in Android resources;
- atomic token/payment transactions and immutable server audit records;
- privacy, consent, retention and deletion policies;
- production signing keys stored outside the repository and CI logs;
- dependency, static-analysis and penetration-test review;
- removal of demo credentials and synthetic data.

The Internet permission must be restored only with the audited production network client and domain policy.