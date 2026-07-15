package com.dolo.doctor.data

import android.content.SharedPreferences
import com.dolo.doctor.data.model.*

interface DoctorStateStore {
    fun restore(defaultState: DoctorUiState): DoctorUiState
    fun save(state: DoctorUiState): Boolean
}

object NoOpDoctorStateStore : DoctorStateStore {
    override fun restore(defaultState: DoctorUiState): DoctorUiState = defaultState
    override fun save(state: DoctorUiState): Boolean = true
}

class SharedPreferencesDoctorStateStore(private val preferences: SharedPreferences) : DoctorStateStore {
    override fun restore(defaultState: DoctorUiState): DoctorUiState {
        if (!preferences.getBoolean(KEY_INITIALIZED, false)) return defaultState
        val schemaVersion = preferences.getInt(KEY_SCHEMA_VERSION, 1)

        val appointmentStatuses = enumMap<AppointmentStatus>(KEY_APPOINTMENT_STATUSES)
        val currentAppointments = if (preferences.contains(KEY_CURRENT_APPOINTMENTS)) {
            preferences.getStringSet(KEY_CURRENT_APPOINTMENTS, emptySet()).orEmpty()
                .mapNotNull(QueueStateCodec::decodeAppointment)
                .sortedBy { it.token }
        } else {
            defaultState.appointments.map { appointment ->
                appointment.copy(status = appointmentStatuses[appointment.id] ?: appointment.status)
            }
        }
        val history = preferences.getStringSet(KEY_QUEUE_HISTORY, emptySet()).orEmpty()
            .mapNotNull(QueueStateCodec::decodeHistory)
            .sortedByDescending { it.date }
        val auditEvents = preferences.getStringSet(KEY_AUDIT_EVENTS, emptySet()).orEmpty()
            .mapNotNull(QueueStateCodec::decodeAuditEvent)
            .sortedBy { it.sequence }
        val profile = preferences.getString(KEY_DOCTOR_PROFILE, null)
            ?.let(QueueStateCodec::decodeProfile)
            ?: defaultState.profile
        val decodedClinics = preferences.getStringSet(KEY_CLINICS, emptySet()).orEmpty()
            .mapNotNull(QueueStateCodec::decodeClinic)
        val clinics = decodedClinics.ifEmpty { defaultState.clinics }
        val feeReadyAppointments = currentAppointments.map { appointment ->
            if (appointment.paymentStatus != PaymentStatus.PENDING && appointment.consultationFee == 0) {
                appointment.copy(consultationFee = profile.consultationFee)
            } else appointment
        }
        val migratedAppointments = if (schemaVersion < 2) {
            migrateIndependentSessionTokens(feeReadyAppointments, preferences.getString(KEY_QUEUE_DATE, defaultState.queueDate) ?: defaultState.queueDate)
        } else feeReadyAppointments
        val activeAnnouncements = preferences.getStringSet(KEY_ACTIVE_ANNOUNCEMENTS, emptySet()).orEmpty()
        val enabledAvailability = preferences.getStringSet(KEY_ENABLED_AVAILABILITY, emptySet()).orEmpty()
        val availabilityBlocks = if (preferences.contains(KEY_AVAILABILITY_BLOCKS)) {
            preferences.getStringSet(KEY_AVAILABILITY_BLOCKS, emptySet()).orEmpty()
                .mapNotNull(QueueStateCodec::decodeAvailabilityBlock)
                .sortedWith(compareBy({ it.fromDate }, { it.sessions }))
        } else {
            defaultState.availabilityBlocks.map { block ->
                block.copy(appointmentsEnabled = block.id in enabledAvailability)
            }
        }
        val permissionEntries = preferences.getStringSet(KEY_ASSISTANT_PERMISSIONS, emptySet()).orEmpty()
        val permissionsByAssistant = permissionEntries.mapNotNull { entry ->
            val (id, value) = entry.pair() ?: return@mapNotNull null
            val permission = runCatching { Permission.valueOf(value) }.getOrNull() ?: return@mapNotNull null
            id to permission
        }.groupBy({ it.first }, { it.second })

        val queueState = preferences.getString(KEY_QUEUE_STATE, null)
            ?.let { runCatching { QueueState.valueOf(it) }.getOrNull() }
            ?: defaultState.queueState
        val currentToken = preferences.getInt(KEY_CURRENT_TOKEN, defaultState.currentToken)
        val decodedSessionQueues = preferences.getStringSet(KEY_SESSION_QUEUES, emptySet()).orEmpty()
            .mapNotNull(QueueStateCodec::decodeSessionQueue)
        val restoredSessionQueues = if (decodedSessionQueues.isNotEmpty()) {
            listOf("Morning", "Evening").map { session ->
                decodedSessionQueues.firstOrNull { it.session == session } ?: ConsultationQueue(session, QueueState.NOT_STARTED, 0)
            }
        } else {
            listOf(
                ConsultationQueue("Morning", queueState, currentToken),
                ConsultationQueue("Evening", QueueState.NOT_STARTED, 0)
            )
        }

val sessionQueues = if (schemaVersion < 2) restoredSessionQueues.map { queue ->
            val progressedToken = migratedAppointments.filter { it.session == queue.session && it.paymentStatus != PaymentStatus.PENDING && it.status in setOf(AppointmentStatus.IN_CONSULTATION, AppointmentStatus.COMPLETED) }.maxOfOrNull { it.token } ?: 0
            queue.copy(currentToken = progressedToken)
        } else restoredSessionQueues

        return defaultState.copy(
            profile = profile,
            clinics = clinics,
            queueDate = preferences.getString(KEY_QUEUE_DATE, defaultState.queueDate) ?: defaultState.queueDate,
            queueHistory = history,
            auditEvents = auditEvents,
            sessionQueues = sessionQueues,
            queueState = sessionQueues.first { it.session == "Morning" }.state,
            currentToken = sessionQueues.first { it.session == "Morning" }.currentToken,
            appointments = migratedAppointments,
            selectedSession = preferences.getString(KEY_SELECTED_SESSION, "Morning").takeIf { it in setOf("Morning", "Evening") } ?: "Morning",
            notificationReadThrough = preferences.getInt(KEY_NOTIFICATION_READ_THROUGH, 0),
            announcements = defaultState.announcements.map { announcement ->
                announcement.copy(active = announcement.id in activeAnnouncements)
            },
            availabilityBlocks = availabilityBlocks,
            assistants = defaultState.assistants.map { assistant ->
                val saved = permissionsByAssistant[assistant.id]?.toSet() ?: emptySet()
                val migrated = if (schemaVersion < 2 && Permission.GENERATE_TOKEN_RECEIPT in saved) saved + Permission.CONFIRM_CONSULTATION_FEE else saved
                assistant.copy(permissions = migrated)
            }
        )
    }

    override fun save(state: DoctorUiState): Boolean = preferences.edit()
        .putBoolean(KEY_INITIALIZED, true)
        .putInt(KEY_SCHEMA_VERSION, 3)
        .putString(KEY_DOCTOR_PROFILE, QueueStateCodec.encodeProfile(state.profile))
        .putStringSet(KEY_CLINICS, state.clinics.mapTo(mutableSetOf(), QueueStateCodec::encodeClinic))
        .putString(KEY_QUEUE_DATE, state.queueDate)
        .putString(KEY_QUEUE_STATE, state.queueState.name)
        .putInt(KEY_CURRENT_TOKEN, state.currentToken)
        .putString(KEY_SELECTED_SESSION, state.selectedSession)
        .putInt(KEY_NOTIFICATION_READ_THROUGH, state.notificationReadThrough)
        .putStringSet(KEY_SESSION_QUEUES, state.sessionQueues.mapTo(mutableSetOf(), QueueStateCodec::encodeSessionQueue))
        .putStringSet(KEY_CURRENT_APPOINTMENTS, state.appointments.mapTo(mutableSetOf(), QueueStateCodec::encodeAppointment))
        .putStringSet(KEY_QUEUE_HISTORY, state.queueHistory.mapTo(mutableSetOf(), QueueStateCodec::encodeHistory))
        .putStringSet(KEY_AUDIT_EVENTS, state.auditEvents.takeLast(500).mapTo(mutableSetOf(), QueueStateCodec::encodeAuditEvent))
        .putStringSet(KEY_APPOINTMENT_STATUSES, state.appointments.mapTo(mutableSetOf()) { "${it.id}|${it.status.name}" })
        .putStringSet(KEY_ACTIVE_ANNOUNCEMENTS, state.announcements.filter { it.active }.mapTo(mutableSetOf()) { it.id })
        .putStringSet(KEY_ENABLED_AVAILABILITY, state.availabilityBlocks.filter { it.appointmentsEnabled }.mapTo(mutableSetOf()) { it.id })
        .putStringSet(KEY_AVAILABILITY_BLOCKS, state.availabilityBlocks.mapTo(mutableSetOf(), QueueStateCodec::encodeAvailabilityBlock))
        .putStringSet(KEY_ASSISTANT_PERMISSIONS, state.assistants.flatMapTo(mutableSetOf()) { assistant -> assistant.permissions.map { "${assistant.id}|${it.name}" } })
        .commit()

    private fun migrateIndependentSessionTokens(appointments: List<Appointment>, queueDate: String): List<Appointment> =
        listOf("Morning", "Evening").flatMap { session ->
            var admittedOrder = 0
            appointments.filter { it.session == session }
                .sortedWith(compareBy<Appointment> { it.token }.thenBy { it.bookedAt })
                .mapIndexed { index, appointment ->
                    val admitted = appointment.paymentStatus != PaymentStatus.PENDING && appointment.receiptNumber.isNotBlank()
                    if (admitted) admittedOrder++
                    val token = index + 1
                    appointment.copy(
                        token = token,
                        queueOrder = if (admitted) admittedOrder else 0,
                        receiptNumber = if (admitted) migratedReceiptNumber(queueDate, session, token) else ""
                    )
                }
        }

    private fun migratedReceiptNumber(queueDate: String, session: String, token: Int): String =
        "DL-" + queueDate.replace("-", "") + "-" + session.first().uppercaseChar() + "-" + token.toString().padStart(3, '0')
    private inline fun <reified T : Enum<T>> enumMap(key: String): Map<String, T> = preferences
        .getStringSet(key, emptySet()).orEmpty()
        .mapNotNull { entry ->
            val (id, value) = entry.pair() ?: return@mapNotNull null
            val enumValue = runCatching { enumValueOf<T>(value) }.getOrNull() ?: return@mapNotNull null
            id to enumValue
        }.toMap()

    private fun String.pair(): Pair<String, String>? {
        val separator = indexOf('|')
        if (separator <= 0 || separator == lastIndex) return null
        return substring(0, separator) to substring(separator + 1)
    }

    private companion object {
        const val KEY_INITIALIZED = "doctor_state_initialized"
        const val KEY_SCHEMA_VERSION = "doctor_state_schema_version"
        const val KEY_SELECTED_SESSION = "doctor_selected_session"
        const val KEY_NOTIFICATION_READ_THROUGH = "doctor_notification_read_through"
        const val KEY_DOCTOR_PROFILE = "doctor_profile"
        const val KEY_CLINICS = "doctor_clinics"
        const val KEY_QUEUE_DATE = "doctor_queue_date"
        const val KEY_QUEUE_STATE = "doctor_queue_state"
        const val KEY_CURRENT_TOKEN = "doctor_current_token"
        const val KEY_SESSION_QUEUES = "doctor_session_queues"
        const val KEY_CURRENT_APPOINTMENTS = "doctor_current_appointments"
        const val KEY_QUEUE_HISTORY = "doctor_queue_history"
        const val KEY_AUDIT_EVENTS = "doctor_queue_audit_events"
        const val KEY_APPOINTMENT_STATUSES = "doctor_appointment_statuses"
        const val KEY_ACTIVE_ANNOUNCEMENTS = "doctor_active_announcements"
        const val KEY_ENABLED_AVAILABILITY = "doctor_enabled_availability"
        const val KEY_AVAILABILITY_BLOCKS = "doctor_availability_blocks"
        const val KEY_ASSISTANT_PERMISSIONS = "doctor_assistant_permissions"
    }
}
