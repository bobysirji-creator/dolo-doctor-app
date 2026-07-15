# DO-LO Doctor App Product Requirements

## Purpose

Give doctors and their authorized assistants a fast mobile workspace for controlling walk-in appointment queues while keeping patients informed through the Patient App.

## Roles

### Doctor

Full ownership of profile, clinics, schedules, queue, appointment availability, announcements and assistant permissions.

### Assistant

Individual credentials with backend-enforced modular permissions. Assistants must never inherit doctor ownership, verification, settlement, credential or staff-management rights by default.

## Core workflows

- review today's appointments;
- close and archive a dated queue, then review immutable daily appointment history;
- start, pause, resume and close a clinic queue;
- call next token and update arrival, waiting, consultation, completion, absence or skipped status;
- configure clinics, sessions, token capacity and average consultation time;
- disable appointments for selected dates, sessions or ranges;
- publish expiring availability notices, health camps, offers and general updates;
- add assistants and define permissions;
- update doctor profile, with Admin approval for sensitive verification fields.

## Cross-app rules

- Patient tokens and queue state become server-authoritative when the backend is connected.
- Queue rollover uses the clinic timezone; each doctor, clinic and date receives a separate queue session and token sequence.
- Doctor availability blocks must disable affected Patient App booking dates.
- Existing affected appointments must be notified and offered a controlled reschedule path; they must not silently disappear.
- Active doctor announcements appear under that doctor's Patient App profile and expire automatically.
- Admin broadcasts target all or selected patients through the shared backend.
- Every status, availability, announcement and permission change requires an audit event.

## Stage 1 limitations

All data and controls are local demonstrations. No real user, patient record, medical note, outbound message, payment, map or production API is used.