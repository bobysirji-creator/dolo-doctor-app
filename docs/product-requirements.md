## Doctor updates and Patient App profile feed

Doctors can create availability notices, health camps, offers and general updates with a title, patient-facing message, start date, end date and draft/published choice. Invalid content and reversed or malformed date ranges must be rejected.

A published update is scheduled before its start date, live throughout its inclusive date range and expired afterward. Only live updates are eligible for the Patient App doctor-profile feed; drafts, future updates and expired updates must never leak into the public projection.

Every create, edit, publish, draft and delete operation requires permission enforcement and an audit record. The shared backend will later own clinic-timezone evaluation, Patient App synchronization and delivery; the Android app stores no privileged broadcast credentials.

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
- close Morning and Evening independently, then review immutable daily history after both sessions close;
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

Token numbering is independent per session; session plus token forms the patient-facing identity. Staff must explicitly select Morning or Evening for every clinic walk-in so it joins the correct queue.

A session accepts advance bookings from the beginning of the clinic day; its configured start time does not act as an enable gate. New bookings close when that session ends, its configured token capacity is reached, or the Doctor manually closes that session. Closing Morning must not close Evening, and the dated history is finalized after both close. At the next clinic-date rollover both session queues reset to NOT_STARTED and become bookable again until their respective end times. Existing consultations may still be completed after the booking cutoff.

## Consultation fee and queue admission

Every appointment has a payment status: PENDING, PAID or WAIVED. Payment methods currently supported by the local workflow are CASH, UPI, CARD, ONLINE and WAIVED. A Patient App booking may appear in Today's appointments with a session token while payment remains pending, but it must not appear in Live queue or be callable.

Authorized clinic staff confirm the fee amount and payment method only after payment is received, or explicitly select WAIVED. That confirmation marks the patient ARRIVED, assigns the next service order in the selected session, generates the stable receipt and admits the appointment to Live queue. Receipt generation without fee confirmation is not allowed. Both the app receipt preview and printed receipt display the amount/status and payment method.

Morning and Evening use separate token sequences beginning independently for each clinic day. Token M-1 and token E-1 may coexist because session is part of the identity and receipt number. The selected Morning/Evening view is shared by Appointments and Queue and remains selected after navigation or process recreation.

## Doctor notifications

The Doctor App provides a persistent notification center and unread Home badge for queue, appointment, fee, receipt and session activity. Local notifications are derived from the persisted audit stream. Remote Patient App events, Admin broadcasts and Android push delivery require the shared backend and provider integration.

## Appointment availability and affected patients

A Doctor can disable new appointments for one clinic, an ISO date or date range, and Morning, Evening or both sessions, with a staff-facing reason. Reopening or deleting a block must restore booking only when no other active rule, session closure, time cutoff or capacity limit prevents it.

An availability change never silently removes an existing appointment. Covered non-terminal appointments require an explicit follow-up state: contact pending, patient notified, reschedule required or resolved. Until resolved, the clinic queue must not call or progress that appointment. All block and follow-up mutations require audit records and future backend notification delivery.

The shared backend remains responsible for authoritative clinic-timezone evaluation, preventing race-condition bookings, informing the Patient App and offering eligible reschedule dates. Stage 6 implements the complete local Doctor workflow and provider-neutral contracts without sending real messages.
