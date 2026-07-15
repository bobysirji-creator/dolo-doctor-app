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

class DoctorViewModel(
    private val stateStore: DoctorStateStore = NoOpDoctorStateStore,
    private val currentDate: () -> LocalDate = LocalDate::now,
    private val currentTime: () -> LocalTime = LocalTime::now
) : ViewModel() {
    var uiState by mutableStateOf(stateStore.restore(DummyData.initialState(currentDate().toString())))
        private set

    private val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a")

    init {
        rollOverIfNeeded()
    }

    private fun currentStateWithout(removedAssistantIds: Set<String>): DoctorUiState =
        uiState.copy(assistants = uiState.assistants.filterNot { it.id in removedAssistantIds })

    private fun persist(state: DoctorUiState) {
        uiState = state
        stateStore.save(state)
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
        AppointmentStatus.SKIPPED -> to in setOf(AppointmentStatus.WAITING, AppointmentStatus.IN_CONSULTATION, AppointmentStatus.ABSENT)
        AppointmentStatus.COMPLETED, AppointmentStatus.ABSENT -> false
    }

    private fun archivedHistory(state: DoctorUiState, closureReason: String, includeEmpty: Boolean = false): List<DailyQueueHistory> {
        if (!includeEmpty && state.appointments.isEmpty() && state.currentToken == 0) return state.queueHistory
        if (state.queueState == QueueState.CLOSED && state.queueHistory.any { it.date == state.queueDate }) {
            return state.queueHistory
        }
        val record = DailyQueueHistory(
            date = state.queueDate,
            clinicName = state.clinics.firstOrNull()?.name ?: "Clinic",
            closedAt = currentTime().format(timeFormatter),
            closureReason = closureReason,
            finalToken = state.currentToken,
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

    fun toggleQueue() {
        rollOverIfNeeded()
        if (!hasPermission(Permission.UPDATE_QUEUE)) return
        val previous = uiState.queueState
        val next = when (previous) {
            QueueState.ACTIVE -> QueueState.PAUSED
            QueueState.PAUSED, QueueState.NOT_STARTED -> QueueState.ACTIVE
            QueueState.CLOSED -> QueueState.CLOSED
        }
        if (next == previous) return
        val action = when {
            previous == QueueState.NOT_STARTED -> AuditAction.QUEUE_STARTED
            next == QueueState.PAUSED -> AuditAction.QUEUE_PAUSED
            else -> AuditAction.QUEUE_RESUMED
        }
        persist(withAudit(uiState.copy(queueState = next), action, "Queue changed from " + previous.name + " to " + next.name))
    }
    fun callNext() {
        rollOverIfNeeded()
        if (uiState.queueState != QueueState.ACTIVE || !hasPermission(Permission.CALL_NEXT_PATIENT)) return
        val current = uiState.appointments.firstOrNull {
            it.token == uiState.currentToken && it.status == AppointmentStatus.IN_CONSULTATION
        }
        val next = uiState.appointments
            .filter { it.token > uiState.currentToken && it.status !in setOf(AppointmentStatus.ABSENT, AppointmentStatus.COMPLETED) }
            .minByOrNull { it.token }

        if (next == null) {
            if (current != null) {
                val updatedAppointments = uiState.appointments.map {
                    if (it.id == current.id) it.copy(status = AppointmentStatus.COMPLETED) else it
                }
                val updated = uiState.copy(appointments = updatedAppointments)
                persist(withAudit(updated, AuditAction.CONSULTATION_COMPLETED, "Completed the final consultation", current, AppointmentStatus.IN_CONSULTATION, AppointmentStatus.COMPLETED))
            }
            return
        }

        val updatedAppointments = uiState.appointments.map { appointment ->
            when {
                appointment.id == current?.id -> appointment.copy(status = AppointmentStatus.COMPLETED)
                appointment.id == next.id -> appointment.copy(status = AppointmentStatus.IN_CONSULTATION)
                else -> appointment
            }
        }
        var updated = uiState.copy(appointments = updatedAppointments, currentToken = next.token)
        if (current != null) {
            updated = withAudit(updated, AuditAction.CONSULTATION_COMPLETED, "Completed consultation before calling the next token", current, AppointmentStatus.IN_CONSULTATION, AppointmentStatus.COMPLETED)
        }
        updated = withAudit(updated, AuditAction.PATIENT_CALLED, "Called token " + next.token, next, next.status, AppointmentStatus.IN_CONSULTATION)
        persist(updated)
    }
    fun updateAppointment(id: String, status: AppointmentStatus) {
        rollOverIfNeeded()
        if (uiState.queueState == QueueState.CLOSED) return
        val required = when (status) {
            AppointmentStatus.ARRIVED -> Permission.MARK_PATIENT_ARRIVED
            AppointmentStatus.ABSENT -> Permission.MARK_PATIENT_ABSENT
            AppointmentStatus.COMPLETED -> Permission.MARK_PATIENT_COMPLETED
            else -> Permission.UPDATE_QUEUE
        }
        if (!hasPermission(required)) return
        val appointment = uiState.appointments.firstOrNull { it.id == id } ?: return
        if (!canTransition(appointment.status, status)) return

        val changed = appointment.copy(status = status)
        val updated = uiState.copy(appointments = uiState.appointments.map { if (it.id == id) changed else it })
        val action = if (status == AppointmentStatus.COMPLETED) AuditAction.CONSULTATION_COMPLETED else AuditAction.STATUS_CHANGED
        persist(withAudit(updated, action, "Changed token " + appointment.token + " from " + appointment.status.name + " to " + status.name, appointment, appointment.status, status))
    }
    fun closeDay(): Boolean {
        rollOverIfNeeded()
        if (uiState.role != UserRole.DOCTOR || uiState.queueState == QueueState.CLOSED) return false
        val closed = uiState.copy(
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
            cleaned.morningSession.length < 5 -> "Enter the morning consultation session."
            cleaned.eveningSession.length < 5 -> "Enter the evening consultation session."
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
