package com.dolo.doctor.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dolo.doctor.data.model.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class DoctorViewModel(
    private val stateStore: DoctorStateStore = NoOpDoctorStateStore,
    private val currentDate: () -> LocalDate = LocalDate::now,
    private val currentTime: () -> LocalTime = LocalTime::now
) : ViewModel() {
    var uiState by mutableStateOf(stateStore.restore(DummyData.initialState(currentDate().toString())))
        private set

    private val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)

    init {
        rollOverIfNeeded()
    }

    private fun currentStateWithout(removedAssistantIds: Set<String>): DoctorUiState =
        uiState.copy(assistants = uiState.assistants.filterNot { it.id in removedAssistantIds })

    private fun persist(state: DoctorUiState) {
        uiState = state
        stateStore.save(state)
    }

    fun queueFor(session: String): ConsultationQueue = uiState.sessionQueues.firstOrNull { it.session == session }
        ?: if (session == "Morning") ConsultationQueue("Morning", uiState.queueState, uiState.currentToken)
        else ConsultationQueue(session, QueueState.NOT_STARTED, 0)

    private fun withSessionQueue(state: DoctorUiState, queue: ConsultationQueue): DoctorUiState {
        val queues = listOf("Morning", "Evening").map { session ->
            if (session == queue.session) queue
            else state.sessionQueues.firstOrNull { it.session == session }
                ?: if (session == "Morning") ConsultationQueue("Morning", state.queueState, state.currentToken)
                else ConsultationQueue(session, QueueState.NOT_STARTED, 0)
        }
        val morning = queues.first { it.session == "Morning" }
        return state.copy(sessionQueues = queues, queueState = morning.state, currentToken = morning.currentToken)
    }
    private fun admittedToQueue(appointment: Appointment): Boolean =
        appointment.paymentStatus != PaymentStatus.PENDING && appointment.receiptNumber.isNotBlank()

    fun selectSession(session: String) {
        if (session !in setOf("Morning", "Evening") || session == uiState.selectedSession) return
        persist(uiState.copy(selectedSession = session))
    }
    private fun actorName(): String = when (uiState.role) {
        UserRole.DOCTOR -> uiState.profile.name
        UserRole.ASSISTANT -> uiState.assistants.firstOrNull { it.id == uiState.activeAssistantId }?.name ?: "Assistant"
        null -> "System"
    }

    private fun withAudit(
        state: DoctorUiState,
        action: AuditAction,
        detail: String,
        appointment: Appointment? = null,
        fromStatus: AppointmentStatus? = null,
        toStatus: AppointmentStatus? = null,
        actorOverride: String? = null
    ): DoctorUiState {
        val sequence = (state.auditEvents.maxOfOrNull { it.sequence } ?: 0) + 1
        val event = QueueAuditEvent(
            id = currentDate().toString() + "-" + sequence,
            sequence = sequence,
            date = currentDate().toString(),
            time = currentTime().format(timeFormatter),
            actor = actorOverride ?: actorName(),
            action = action,
            token = appointment?.token,
            patientName = appointment?.patientName,
            fromStatus = fromStatus,
            toStatus = toStatus,
            detail = detail
        )
        return state.copy(auditEvents = (state.auditEvents + event).takeLast(500))
    }

    private fun canTransition(from: AppointmentStatus, to: AppointmentStatus): Boolean = when (from) {
        AppointmentStatus.BOOKED -> to in setOf(AppointmentStatus.ARRIVED, AppointmentStatus.ABSENT)
        AppointmentStatus.ARRIVED -> to in setOf(AppointmentStatus.WAITING, AppointmentStatus.IN_CONSULTATION, AppointmentStatus.ABSENT)
        AppointmentStatus.WAITING -> to in setOf(AppointmentStatus.IN_CONSULTATION, AppointmentStatus.ABSENT, AppointmentStatus.SKIPPED)
        AppointmentStatus.IN_CONSULTATION -> to in setOf(AppointmentStatus.COMPLETED, AppointmentStatus.SKIPPED)
        AppointmentStatus.SKIPPED -> to == AppointmentStatus.ABSENT
        AppointmentStatus.COMPLETED, AppointmentStatus.ABSENT -> false
    }

    private fun archivedHistory(state: DoctorUiState, closureReason: String, includeEmpty: Boolean = false): List<DailyQueueHistory> {
        if (!includeEmpty && state.appointments.isEmpty() && state.sessionQueues.all { it.currentToken == 0 }) return state.queueHistory
        if (state.queueState == QueueState.CLOSED && state.queueHistory.any { it.date == state.queueDate }) {
            return state.queueHistory
        }
        val record = DailyQueueHistory(
            date = state.queueDate,
            clinicName = state.clinics.firstOrNull()?.name ?: "Clinic",
            closedAt = currentTime().format(timeFormatter),
            closureReason = closureReason,
            finalToken = state.sessionQueues.maxOfOrNull { it.currentToken } ?: state.currentToken,
            appointments = state.appointments.map { it.copy() }
        )
        return (state.queueHistory.filterNot { it.date == state.queueDate } + record)
            .sortedByDescending { it.date }
    }

    private fun rollOverIfNeeded() {
        val today = currentDate().toString()
        if (uiState.queueDate >= today) return
        val previousDate = uiState.queueDate
        val rolled = uiState.copy(
            queueDate = today,
            queueHistory = archivedHistory(uiState, "Automatic date rollover"),
            appointments = emptyList(),
            sessionQueues = listOf(
                ConsultationQueue("Morning", QueueState.NOT_STARTED, 0),
                ConsultationQueue("Evening", QueueState.NOT_STARTED, 0)
            ),
            queueState = QueueState.NOT_STARTED,
            currentToken = 0
        )
        persist(withAudit(rolled, AuditAction.DAY_ROLLED_OVER, "Started a new queue day after " + previousDate, actorOverride = "System"))
    }
    fun login(role: UserRole, assistantId: String? = null, removedAssistantIds: Set<String> = emptySet()) {
        rollOverIfNeeded()
        uiState = currentStateWithout(removedAssistantIds).copy(
            role = role,
            activeAssistantId = if (role == UserRole.ASSISTANT) assistantId else null
        )
    }

    fun logout(removedAssistantIds: Set<String> = emptySet()) {
        rollOverIfNeeded()
        uiState = currentStateWithout(removedAssistantIds).copy(
            role = null,
            activeAssistantId = null
        )
    }

    fun permissions(): Set<Permission> = when (uiState.role) {
        UserRole.DOCTOR -> Permission.entries.toSet()
        UserRole.ASSISTANT -> uiState.assistants.firstOrNull { it.id == uiState.activeAssistantId }?.permissions.orEmpty()
        null -> emptySet()
    }

    fun hasPermission(permission: Permission): Boolean = permission in permissions()

    fun toggleQueue(session: String = "Morning") {
        rollOverIfNeeded()
        if (!hasPermission(Permission.UPDATE_QUEUE)) return
        val queue = queueFor(session)
        val next = when (queue.state) {
            QueueState.ACTIVE -> QueueState.PAUSED
            QueueState.PAUSED, QueueState.NOT_STARTED -> QueueState.ACTIVE
            QueueState.CLOSED -> QueueState.CLOSED
        }
        if (next == queue.state) return
        val action = when {
            queue.state == QueueState.NOT_STARTED -> AuditAction.QUEUE_STARTED
            next == QueueState.PAUSED -> AuditAction.QUEUE_PAUSED
            else -> AuditAction.QUEUE_RESUMED
        }
        val updated = withSessionQueue(uiState, queue.copy(state = next))
        persist(withAudit(updated, action, session + " queue changed from " + queue.state.name + " to " + next.name))
    }

    fun callNext(session: String = "Morning") {
        rollOverIfNeeded()
        val queue = queueFor(session)
        if (queue.state != QueueState.ACTIVE || !hasPermission(Permission.CALL_NEXT_PATIENT)) return
        val current = uiState.appointments.firstOrNull {
            it.session == session && admittedToQueue(it) && it.token == queue.currentToken && it.status == AppointmentStatus.IN_CONSULTATION
        }
        val next = uiState.appointments
            .filter { it.session == session && admittedToQueue(it) && it.status in setOf(AppointmentStatus.ARRIVED, AppointmentStatus.WAITING) }
            .minByOrNull { it.queueOrder }

        if (next == null) {
            if (current != null) {
                val appointments = uiState.appointments.map { if (it.id == current.id) it.copy(status = AppointmentStatus.COMPLETED) else it }
                val updated = uiState.copy(appointments = appointments)
                persist(withAudit(updated, AuditAction.CONSULTATION_COMPLETED, "Completed the final " + session.lowercase() + " consultation", current, AppointmentStatus.IN_CONSULTATION, AppointmentStatus.COMPLETED))
            }
            return
        }

        val appointments = uiState.appointments.map { appointment ->
            when {
                appointment.id == current?.id -> appointment.copy(status = AppointmentStatus.COMPLETED)
                appointment.id == next.id -> appointment.copy(status = AppointmentStatus.IN_CONSULTATION)
                else -> appointment
            }
        }
        var updated = withSessionQueue(uiState.copy(appointments = appointments), queue.copy(currentToken = next.token))
        if (current != null) {
            updated = withAudit(updated, AuditAction.CONSULTATION_COMPLETED, "Completed " + session.lowercase() + " consultation before calling the next token", current, AppointmentStatus.IN_CONSULTATION, AppointmentStatus.COMPLETED)
        }
        updated = withAudit(updated, AuditAction.PATIENT_CALLED, "Called " + session.lowercase() + " token " + next.token, next, next.status, AppointmentStatus.IN_CONSULTATION)
        persist(updated)
    }
    fun updateAppointment(id: String, status: AppointmentStatus) {
        rollOverIfNeeded()
        val required = when (status) {
            AppointmentStatus.ARRIVED -> Permission.MARK_PATIENT_ARRIVED
            AppointmentStatus.ABSENT -> Permission.MARK_PATIENT_ABSENT
            AppointmentStatus.COMPLETED -> Permission.MARK_PATIENT_COMPLETED
            else -> Permission.UPDATE_QUEUE
        }
        if (!hasPermission(required)) return
        val appointment = uiState.appointments.firstOrNull { it.id == id } ?: return
        if (!admittedToQueue(appointment) || queueFor(appointment.session).state == QueueState.CLOSED) return
        if (!canTransition(appointment.status, status)) return

        val progressedOrder = uiState.appointments.filter { it.session == appointment.session && it.status in setOf(AppointmentStatus.IN_CONSULTATION, AppointmentStatus.COMPLETED, AppointmentStatus.SKIPPED) }.maxOfOrNull { it.queueOrder } ?: 0
        val lateArrival = status == AppointmentStatus.ARRIVED && appointment.queueOrder < progressedOrder
        val changed = appointment.copy(
            status = status,
            queueOrder = if (lateArrival) nextQueueOrder(appointment.session) else appointment.queueOrder
        )
        var updated = uiState.copy(appointments = uiState.appointments.map { if (it.id == id) changed else it })
        val action = when {
            status == AppointmentStatus.COMPLETED -> AuditAction.CONSULTATION_COMPLETED
            lateArrival -> AuditAction.PATIENT_REJOINED
            else -> AuditAction.STATUS_CHANGED
        }
        val detail = if (lateArrival) {
            "Late arrival token " + appointment.token + " joined at the end of the active queue"
        } else {
            "Changed token " + appointment.token + " from " + appointment.status.name + " to " + status.name
        }
        updated = withAudit(updated, action, detail, appointment, appointment.status, status)
        persist(updated)
    }

    fun resumeSkippedConsultation(id: String): Boolean {
        rollOverIfNeeded()
        if (!hasPermission(Permission.UPDATE_QUEUE)) return false
        val appointment = uiState.appointments.firstOrNull { it.id == id } ?: return false
        val queue = queueFor(appointment.session)
        if (queue.state == QueueState.CLOSED) return false
        if (uiState.appointments.any { it.session == appointment.session && it.status == AppointmentStatus.IN_CONSULTATION }) return false
        if (appointment.status != AppointmentStatus.SKIPPED) return false
        val resumed = appointment.copy(status = AppointmentStatus.IN_CONSULTATION)
        val updated = withSessionQueue(
            uiState.copy(appointments = uiState.appointments.map { if (it.id == id) resumed else it }),
            queue.copy(currentToken = appointment.token)
        )
        persist(withAudit(updated, AuditAction.PATIENT_REJOINED, "Immediately resumed skipped " + appointment.session.lowercase() + " token " + appointment.token + " in consultation", appointment, AppointmentStatus.SKIPPED, AppointmentStatus.IN_CONSULTATION))
        return true
    }

    fun rejoinAppointment(id: String): Boolean {
        rollOverIfNeeded()
        if (!hasPermission(Permission.UPDATE_QUEUE)) return false
        val appointment = uiState.appointments.firstOrNull { it.id == id } ?: return false
        if (queueFor(appointment.session).state == QueueState.CLOSED || appointment.status != AppointmentStatus.SKIPPED) return false
        val rejoined = appointment.copy(status = AppointmentStatus.WAITING, queueOrder = nextQueueOrder(appointment.session))
        val updated = uiState.copy(appointments = uiState.appointments.map { if (it.id == id) rejoined else it })
        persist(withAudit(updated, AuditAction.PATIENT_REJOINED, "Rejoined skipped " + appointment.session.lowercase() + " token " + appointment.token + " at the end of its session queue", appointment, AppointmentStatus.SKIPPED, AppointmentStatus.WAITING))
        return true
    }
    fun bookWalkIn(request: WalkInBookingRequest): WalkInBookingResult {
        rollOverIfNeeded()
        if (!hasPermission(Permission.BOOK_WALK_IN_APPOINTMENT) || !hasPermission(Permission.CONFIRM_CONSULTATION_FEE)) return WalkInBookingResult(error = "This account cannot collect fees and book walk-in patients.")
        val name = request.patientName.trim()
        val phone = request.patientPhone.filter(Char::isDigit)
        val patientType = request.patientType.trim()
        val session = request.session.trim()
        val clinic = uiState.clinics.firstOrNull() ?: return WalkInBookingResult(error = "Clinic details are unavailable.")
        val fee = if (request.paymentMethod == PaymentMethod.WAIVED) 0 else request.consultationFee.takeIf { it > 0 } ?: uiState.profile.consultationFee
        val error = when {
            queueFor(session).state == QueueState.CLOSED -> session + " queue is closed."
            !sessionBookingOpen(session) -> session + " booking has closed because the session end time has passed."
            name.length < 3 -> "Enter the patient's full name."
            phone.length != 10 -> "Enter a valid 10-digit mobile number."
            patientType.isBlank() -> "Select the patient type."
            session !in setOf("Morning", "Evening") -> "Select Morning or Evening session."
            fee !in 0..100000 || (request.paymentMethod != PaymentMethod.WAIVED && fee == 0) -> "Enter a valid fee or select Waived."
            uiState.appointments.count { it.session == session } >= clinic.maxTokensPerSession -> "The selected session has reached its token limit."
            else -> null
        }
        if (error != null) return WalkInBookingResult(error = error)
        val token = (uiState.appointments.filter { it.session == session }.maxOfOrNull { it.token } ?: 0) + 1
        val paymentStatus = if (request.paymentMethod == PaymentMethod.WAIVED) PaymentStatus.WAIVED else PaymentStatus.PAID
        val appointment = Appointment(
            id = "walkin-" + uiState.queueDate + "-" + session.first() + "-" + token + "-" + currentTime().toSecondOfDay(), token = token,
            patientName = name, patientType = patientType, session = session, status = AppointmentStatus.ARRIVED,
            bookedAt = currentTime().format(timeFormatter), queueOrder = nextQueueOrder(session), bookingSource = BookingSource.CLINIC_WALK_IN,
            patientPhone = phone, receiptNumber = receiptNumber(session, token), consultationFee = fee, paymentStatus = paymentStatus,
            paymentMethod = request.paymentMethod, paidAt = currentTime().format(timeFormatter)
        )
        var updated = uiState.copy(appointments = uiState.appointments + appointment)
        updated = withAudit(updated, AuditAction.WALK_IN_BOOKED, "Booked clinic walk-in and allotted " + session.lowercase() + " token " + token, appointment)
        updated = withAudit(updated, AuditAction.FEE_CONFIRMED, "Confirmed " + paymentStatus.name.lowercase() + " consultation fee INR " + fee + " by " + request.paymentMethod.name, appointment)
        updated = withAudit(updated, AuditAction.RECEIPT_GENERATED, "Generated compulsory token receipt " + appointment.receiptNumber, appointment)
        persist(updated)
        return WalkInBookingResult(receipt = tokenReceipt(appointment))
    }

    fun confirmConsultationFee(appointmentId: String, amount: Int, method: PaymentMethod): FeeConfirmationResult {
        rollOverIfNeeded()
        if (!hasPermission(Permission.CONFIRM_CONSULTATION_FEE) || !hasPermission(Permission.GENERATE_TOKEN_RECEIPT)) return FeeConfirmationResult(error = "This account cannot confirm consultation fees.")
        val appointment = uiState.appointments.firstOrNull { it.id == appointmentId } ?: return FeeConfirmationResult(error = "Appointment was not found.")
        if (appointment.paymentStatus != PaymentStatus.PENDING) return FeeConfirmationResult(receipt = receiptFor(appointmentId))
        if (appointment.status != AppointmentStatus.BOOKED) return FeeConfirmationResult(error = "Only a booked appointment can be admitted after fee confirmation.")
        if (queueFor(appointment.session).state == QueueState.CLOSED) return FeeConfirmationResult(error = appointment.session + " queue is closed.")
        val fee = if (method == PaymentMethod.WAIVED) 0 else amount
        if (fee !in 0..100000 || (method != PaymentMethod.WAIVED && fee == 0)) return FeeConfirmationResult(error = "Enter a valid fee or select Waived.")
        val confirmed = appointment.copy(
            status = AppointmentStatus.ARRIVED, queueOrder = nextQueueOrder(appointment.session),
            receiptNumber = receiptNumber(appointment.session, appointment.token), consultationFee = fee,
            paymentStatus = if (method == PaymentMethod.WAIVED) PaymentStatus.WAIVED else PaymentStatus.PAID,
            paymentMethod = method, paidAt = currentTime().format(timeFormatter)
        )
        var updated = uiState.copy(appointments = uiState.appointments.map { if (it.id == appointmentId) confirmed else it })
        updated = withAudit(updated, AuditAction.FEE_CONFIRMED, "Confirmed " + confirmed.paymentStatus.name.lowercase() + " consultation fee INR " + fee + " by " + method.name + " and admitted patient to " + appointment.session.lowercase() + " queue", confirmed, appointment.status, AppointmentStatus.ARRIVED)
        updated = withAudit(updated, AuditAction.RECEIPT_GENERATED, "Generated compulsory token receipt " + confirmed.receiptNumber, confirmed)
        persist(updated)
        return FeeConfirmationResult(receipt = tokenReceipt(confirmed))
    }

    fun receiptFor(appointmentId: String): TokenReceipt? {
        if (!hasPermission(Permission.GENERATE_TOKEN_RECEIPT)) return null
        val appointment = uiState.appointments.firstOrNull { it.id == appointmentId } ?: return null
        if (!admittedToQueue(appointment)) return null
        return tokenReceipt(appointment)
    }
    private fun nextQueueOrder(session: String): Int = (uiState.appointments.filter { it.session == session && admittedToQueue(it) }.maxOfOrNull { it.queueOrder } ?: 0) + 1

    fun refreshDate() = rollOverIfNeeded()

    fun sessionBookingOpen(session: String): Boolean {
        if (session !in setOf("Morning", "Evening")) return false
        if (queueFor(session).state == QueueState.CLOSED) return false
        val clinic = uiState.clinics.firstOrNull() ?: return false
        val schedule = if (session == "Morning") clinic.morningSession else clinic.eveningSession
        val (_, end) = parseSessionRange(schedule) ?: return false
        return currentTime().isBefore(end)
    }

    private fun parseSessionRange(value: String): Pair<LocalTime, LocalTime>? {
        val parts = value.split(" - ", limit = 2)
        if (parts.size != 2) return null
        val start = runCatching { LocalTime.parse(parts[0].trim(), timeFormatter) }.getOrNull() ?: return null
        val end = runCatching { LocalTime.parse(parts[1].trim(), timeFormatter) }.getOrNull() ?: return null
        return (start to end).takeIf { start.isBefore(end) }
    }
    private fun receiptNumber(session: String, token: Int): String = "DL-" + uiState.queueDate.replace("-", "") + "-" + session.first().uppercaseChar() + "-" + token.toString().padStart(3, '0')

    private fun tokenReceipt(appointment: Appointment): TokenReceipt {
        val clinic = uiState.clinics.first()
        return TokenReceipt(
            receiptNumber = appointment.receiptNumber,
            token = appointment.token,
            appointmentDate = uiState.queueDate,
            generatedAt = currentTime().format(timeFormatter),
            patientName = appointment.patientName,
            patientPhone = appointment.patientPhone,
            patientType = appointment.patientType,
            doctorName = uiState.profile.name,
            clinicName = clinic.name,
            clinicAddress = clinic.address,
            session = appointment.session,
            bookingSource = appointment.bookingSource,
            consultationFee = appointment.consultationFee,
            paymentStatus = appointment.paymentStatus,
            paymentMethod = appointment.paymentMethod ?: PaymentMethod.CASH,
            paidAt = appointment.paidAt
        )
    }
    fun closeDay(): Boolean {
        rollOverIfNeeded()
        if (uiState.role != UserRole.DOCTOR || uiState.sessionQueues.all { it.state == QueueState.CLOSED }) return false
        val closed = uiState.copy(
            sessionQueues = uiState.sessionQueues.map { it.copy(state = QueueState.CLOSED) },
            queueState = QueueState.CLOSED,
            queueHistory = archivedHistory(uiState, "Closed manually by doctor", includeEmpty = true)
        )
        persist(withAudit(closed, AuditAction.DAY_CLOSED, "Closed and archived queue day " + uiState.queueDate))
        return true
    }
    fun updateProfile(updated: DoctorProfile): String? {
        if (uiState.role != UserRole.DOCTOR) return "Only the doctor can update the profile."
        val cleaned = updated.copy(
            name = updated.name.trim(),
            specialty = updated.specialty.trim(),
            qualification = updated.qualification.trim(),
            registrationNumber = updated.registrationNumber.trim().uppercase(),
            about = updated.about.trim()
        )
        val error = when {
            cleaned.name.length < 3 -> "Enter the doctor's full name."
            cleaned.specialty.length < 3 -> "Enter a valid specialty."
            cleaned.qualification.length < 2 -> "Enter valid qualifications."
            cleaned.registrationNumber.length < 4 -> "Enter a valid registration number."
            cleaned.experienceYears !in 0..70 -> "Experience must be between 0 and 70 years."
            cleaned.consultationFee !in 0..100000 -> "Consultation fee must be between 0 and 100000."
            cleaned.about.length !in 10..500 -> "About must contain 10 to 500 characters."
            else -> null
        }
        if (error != null) return error

        val current = uiState.profile
        val sensitiveChanged = cleaned.specialty != current.specialty ||
            cleaned.qualification != current.qualification ||
            cleaned.registrationNumber != current.registrationNumber
        persist(
            uiState.copy(
                profile = cleaned.copy(
                    verified = current.verified,
                    reviewStatus = if (sensitiveChanged) ProfileReviewStatus.PENDING_REVIEW else current.reviewStatus
                )
            )
        )
        return null
    }

    fun updateClinic(updated: Clinic): String? {
        if (uiState.role != UserRole.DOCTOR) return "Only the doctor can update clinic details."
        if (uiState.clinics.none { it.id == updated.id }) return "Clinic record was not found."
        val cleaned = updated.copy(
            name = updated.name.trim(),
            address = updated.address.trim(),
            phone = updated.phone.trim(),
            morningSession = updated.morningSession.trim(),
            eveningSession = updated.eveningSession.trim()
        )
        val phoneDigits = cleaned.phone.filter(Char::isDigit)
        val error = when {
            cleaned.name.length < 3 -> "Enter a valid clinic name."
            cleaned.address.length < 8 -> "Enter a complete clinic address."
            phoneDigits.length !in 7..15 -> "Enter a valid clinic phone number."
            parseSessionRange(cleaned.morningSession) == null -> "Use Morning format HH:MM AM - HH:MM PM with end time after start."
            parseSessionRange(cleaned.eveningSession) == null -> "Use Evening format HH:MM AM - HH:MM PM with end time after start."
            cleaned.maxTokensPerSession !in 1..200 -> "Maximum tokens must be between 1 and 200."
            cleaned.averageConsultationMinutes !in 5..120 -> "Average consultation time must be between 5 and 120 minutes."
            else -> null
        }
        if (error != null) return error

        persist(uiState.copy(clinics = uiState.clinics.map { if (it.id == cleaned.id) cleaned else it }))
        return null
    }
    fun toggleAnnouncement(id: String) {
        if (uiState.role != UserRole.DOCTOR) return
        persist(uiState.copy(announcements = uiState.announcements.map { if (it.id == id) it.copy(active = !it.active) else it }))
    }

    fun toggleAppointments(blockId: String) {
        if (uiState.role != UserRole.DOCTOR) return
        persist(uiState.copy(availabilityBlocks = uiState.availabilityBlocks.map {
            if (it.id == blockId) it.copy(appointmentsEnabled = !it.appointmentsEnabled) else it
        }))
    }

    fun deleteAssistant(assistantId: String): Boolean {
        if (uiState.role != UserRole.DOCTOR || uiState.assistants.none { it.id == assistantId }) return false
        persist(uiState.copy(assistants = uiState.assistants.filterNot { it.id == assistantId }))
        return true
    }

    fun togglePermission(assistantId: String, permission: Permission) {
        if (uiState.role != UserRole.DOCTOR) return
        persist(uiState.copy(assistants = uiState.assistants.map { assistant ->
            if (assistant.id != assistantId) assistant else {
                val permissions = assistant.permissions.toMutableSet()
                if (!permissions.add(permission)) permissions.remove(permission)
                assistant.copy(permissions = permissions)
            }
        }))
    }
}

class DoctorViewModelFactory(private val stateStore: DoctorStateStore) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = DoctorViewModel(stateStore) as T
}
