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
## Clinic walk-in booking and compulsory token receipt

Appointments have two sources: PATIENT_APP and CLINIC_WALK_IN. Both sources must enter the same doctor, clinic, date and session queue and receive one unique token sequence. An authorized Doctor or Assistant can register an in-person patient, capture name/mobile/patient type/session, mark the patient arrived, allot the next token and generate the token receipt in one workflow.

A token receipt is compulsory for every arrived patient, whether booked online or at the clinic. It displays the token prominently plus receipt number, patient, doctor, clinic, date, session, source and generation time. The patient hands the physical receipt to the doctor at consultation. Reprints retain the same token and receipt number and must not create another appointment.

Skipped patients are recoverable. If Skip was accidental and no other consultation has started, staff can resume the same patient immediately. Otherwise staff can rejoin the patient at the end of the active queue. A patient arriving after their original turn keeps the original token but receives a new queue position at the end. ABSENT and COMPLETED remain terminal statuses.

## Consultation-session queues and booking cutoff

Morning and Evening are independent operational queues for the same clinic day. Each has its own NOT_STARTED, ACTIVE, PAUSED or CLOSED state, current token and queue order. Starting, pausing, resuming or calling the next patient in one session must not advance or change the other session.

The patient-facing token remains unique across the complete clinic day, including both sessions. Staff must explicitly select Morning or Evening for every clinic walk-in so it joins the correct queue.

A session accepts advance bookings from the beginning of the clinic day; its configured start time does not act as an enable gate. New bookings close automatically when the configured session end time is reached. Closing Morning booking must not close Evening booking. At the next clinic-date rollover both session queues reset to NOT_STARTED and become bookable again until their respective end times. Existing consultations may still be completed after the booking cutoff.
