## Recurring weekly clinic schedule

A Doctor can configure each weekday as open, Morning off, Evening off or full day off. This repeating schedule is clinic-level and separate from exceptional date/date-range Availability blocks.

The rule must prevent new Patient App and clinic walk-in bookings for the affected session/date, prevent fee admission into that session and prevent starting its queue. The other session remains independent. On the next weekday whose session is open, normal booking eligibility resumes automatically without a manual enable action.
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

Every appointment has a clinic-fee record status: PENDING, PAID or WAIVED. Current clinic-record methods are CASH, UPI, CARD and WAIVED. The legacy ONLINE enum is retained only for installed-data compatibility and is not selectable. A Patient App booking may appear in Today's appointments with a session token while clinic confirmation remains pending, but it must not appear in Live queue or be callable.

Authorized clinic staff record the fee amount and method only after the clinic directly receives payment, or explicitly selects WAIVED. That record marks the patient ARRIVED, generates the stable receipt and admits the appointment to Live queue. An upcoming online token is inserted before higher upcoming tokens even if those receipts were generated first. A token whose turn has passed keeps its printed number and is inserted after up to four waiting patients. Late patients admitted while the same token remains in consultation form one cohort ordered by original token number. Receipt generation without clinic confirmation is not allowed. Both the app receipt preview and printed receipt display the clinic-reported amount/status and method.

Morning and Evening use separate token sequences beginning independently for each clinic day. Token M-1 and token E-1 may coexist because session is part of the identity and receipt number. The selected Morning/Evening view is shared by Appointments and Queue and remains selected after navigation or process recreation.

Operational summary and Queue History use an inclusive start/end clinic-date range and default to the current day. Queue History includes the active day before archive and separates detailed Morning and Evening patient records.

Android screens and bottom navigation must respect safe drawing, gesture-navigation and three-button-navigation insets across supported screen sizes.


## Financial boundary: clinic fees versus DO-LO service charges

The doctor's consultation fee is never collected or transferred by DO-LO in the current product model. Patients pay it directly to the doctor/clinic. Authorized staff record Cash, UPI, Card or Waived only after the clinic receives or waives that fee; this local record controls receipt generation and queue admission.

DO-LO service charges are separate from consultation fees. The Patient App will pay a DO-LO service charge online when booking. Doctors will be billed by DO-LO weekly or monthly under an Admin-defined count/rate policy. Payment-gateway orders, reconciliation, invoices, waivers, disputes and settlement controls for both service-charge streams belong to the shared backend and Admin App. The Doctor App may later display read-only bills/invoices but must not treat them as consultation-fee receipts.

## Doctor notifications

The Doctor App provides a persistent notification center and unread Home badge for queue, appointment, fee, receipt and session activity. Local notifications are derived from the persisted audit stream. Remote Patient App events, Admin broadcasts and Android push delivery require the shared backend and provider integration.

## Appointment availability and affected patients

A Doctor can disable new appointments for one clinic, an ISO date or date range, and Morning, Evening or both sessions, with a staff-facing reason. Reopening or deleting a block must restore booking only when no other active rule, session closure, time cutoff or capacity limit prevents it.

An availability change never silently removes an existing appointment. Covered non-terminal appointments require an explicit follow-up state: contact pending, patient notified, reschedule required or resolved. Until resolved, the clinic queue must not call or progress that appointment. All block and follow-up mutations require audit records and future backend notification delivery.

The shared backend remains responsible for authoritative clinic-timezone evaluation, preventing race-condition bookings, informing the Patient App and offering eligible reschedule dates. Stage 6 implements the complete local Doctor workflow and provider-neutral contracts without sending real messages.

## Assistant account lifecycle

Only a Doctor can create, disable, re-enable, reset or delete an assistant account. Each assistant has an individual mobile login, active status and modular permission set. New local accounts receive a generated four-digit temporary PIN that is shown once; readable PINs must not be persisted.

Disabling an account immediately blocks login while retaining its profile and permissions for later reactivation. Resetting a PIN invalidates the previous PIN. Deletion removes the profile and credential and keeps legacy identities revoked. Every lifecycle and permission change requires an audit event.

The local hash registry exists only for workflow testing. The shared backend must issue authoritative credentials, require first-login PIN replacement, rate-limit authentication, revoke sessions across devices and enforce every assistant permission server-side.

## Operational reports, feedback and queue-delay notices

Doctors can view an operational summary derived from current appointments and archived queue days: total appointments, completed, absent, fee pending and confirmed consultation-fee collections. Authorized Assistants require an explicit report permission.

Patient feedback contains clinic identity, patient display name, rating, comment, submission date and acknowledgement state. Viewing and acknowledging detailed feedback requires Doctor ownership or VIEW_PATIENT_FEEDBACK. Acknowledgement creates an audit event but never edits the patient's rating or comment.

A Doctor or Assistant with SEND_QUEUE_DELAY_NOTICE can create a Morning or Evening notice with a 5-to-240-minute delay and patient-facing message. Stage 9 persists the delivery candidate; Stage 10 must select affected Patient App appointments, prevent duplicate delivery, expire stale notices and send push/SMS only through approved backend providers.

Clinic access for Assistants is read-only and permission controlled. Clinic-ID-scoped models and service contracts prepare for multiple clinics, but independent token allocation, queue state and clinic switching must remain backend-authoritative rather than being simulated with conflicting local queues.

## Stage 10 shared backend readiness

The Doctor and Patient apps must exchange clinic-day state only through an authenticated shared backend. Every operational snapshot has a monotonically increasing revision, every mutation has an idempotency key, and stale writes must return a conflict instead of overwriting a newer queue.

Patient App booking must allocate the next token atomically within its clinic/date/session sequence. Online booking starts fee-pending and remains outside the consultation queue until authorized clinic staff confirms payment or waiver and generates the compulsory token receipt.

The Stage 10 local mock exists only to test these rules. It must be visibly identified as non-networked and must not claim cross-device synchronization.
## Future appointment booking policy

Each Doctor controls Patient App booking at clinic level:

- current-day-only; or
- future booking enabled with a maximum window from 1 to 90 days.

Existing clinics default to current-day-only. The backend must publish this policy to the Patient App, use the clinic timezone, reject dates outside the window and apply availability blocks/session capacity after the date policy passes.

Clinic walk-in/offline booking is always for the current clinic day. Enabling Patient App advance booking must never add a future-date option to the Doctor/Assistant walk-in form.

Every policy change is Doctor-only, persisted, auditable and included in shared synchronization.
## Stage 14 local PIN lifecycle

- Doctors and Assistants can change a local login PIN only after proving the current PIN.
- Newly created Assistants and Assistants whose PIN is reset must replace the temporary PIN before clinic access.
- Mandatory replacement survives process death and cannot be bypassed by a previously saved session.
- New PINs use salted PBKDF2-HMAC-SHA256 storage; legacy Assistant hashes migrate after successful login.
- Existing Stage 13 sessions and credentials remain compatible during stable in-place upgrade.
- The local prototype has no Doctor PIN recovery; hosted identity recovery, retry throttling and multi-device revocation remain production requirements.