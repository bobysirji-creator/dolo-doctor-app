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

        val appointmentStatuses = enumMap<AppointmentStatus>(KEY_APPOINTMENT_STATUSES)
        val activeAnnouncements = preferences.getStringSet(KEY_ACTIVE_ANNOUNCEMENTS, emptySet()).orEmpty()
        val enabledAvailability = preferences.getStringSet(KEY_ENABLED_AVAILABILITY, emptySet()).orEmpty()
        val permissionEntries = preferences.getStringSet(KEY_ASSISTANT_PERMISSIONS, emptySet()).orEmpty()
        val permissionsByAssistant = permissionEntries.mapNotNull { entry ->
            val (id, value) = entry.pair() ?: return@mapNotNull null
            val permission = runCatching { Permission.valueOf(value) }.getOrNull() ?: return@mapNotNull null
            id to permission
        }.groupBy({ it.first }, { it.second })

        val queueState = preferences.getString(KEY_QUEUE_STATE, null)
            ?.let { runCatching { QueueState.valueOf(it) }.getOrNull() }
            ?: defaultState.queueState

        return defaultState.copy(
            queueState = queueState,
            currentToken = preferences.getInt(KEY_CURRENT_TOKEN, defaultState.currentToken),
            appointments = defaultState.appointments.map { appointment ->
                appointment.copy(status = appointmentStatuses[appointment.id] ?: appointment.status)
            },
            announcements = defaultState.announcements.map { announcement ->
                announcement.copy(active = announcement.id in activeAnnouncements)
            },
            availabilityBlocks = defaultState.availabilityBlocks.map { block ->
                block.copy(appointmentsEnabled = block.id in enabledAvailability)
            },
            assistants = defaultState.assistants.map { assistant ->
                assistant.copy(permissions = permissionsByAssistant[assistant.id]?.toSet() ?: emptySet())
            }
        )
    }

    override fun save(state: DoctorUiState): Boolean = preferences.edit()
        .putBoolean(KEY_INITIALIZED, true)
        .putString(KEY_QUEUE_STATE, state.queueState.name)
        .putInt(KEY_CURRENT_TOKEN, state.currentToken)
        .putStringSet(KEY_APPOINTMENT_STATUSES, state.appointments.mapTo(mutableSetOf()) { "${it.id}|${it.status.name}" })
        .putStringSet(KEY_ACTIVE_ANNOUNCEMENTS, state.announcements.filter { it.active }.mapTo(mutableSetOf()) { it.id })
        .putStringSet(KEY_ENABLED_AVAILABILITY, state.availabilityBlocks.filter { it.appointmentsEnabled }.mapTo(mutableSetOf()) { it.id })
        .putStringSet(KEY_ASSISTANT_PERMISSIONS, state.assistants.flatMapTo(mutableSetOf()) { assistant -> assistant.permissions.map { "${assistant.id}|${it.name}" } })
        .commit()

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
        const val KEY_QUEUE_STATE = "doctor_queue_state"
        const val KEY_CURRENT_TOKEN = "doctor_current_token"
        const val KEY_APPOINTMENT_STATUSES = "doctor_appointment_statuses"
        const val KEY_ACTIVE_ANNOUNCEMENTS = "doctor_active_announcements"
        const val KEY_ENABLED_AVAILABILITY = "doctor_enabled_availability"
        const val KEY_ASSISTANT_PERMISSIONS = "doctor_assistant_permissions"
    }
}