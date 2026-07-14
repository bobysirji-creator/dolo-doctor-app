package com.dolo.doctor.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.dolo.doctor.data.model.*

class DoctorViewModel : ViewModel() {
    var uiState by mutableStateOf(DummyData.initialState())
        private set

    fun login(role: UserRole, assistantId: String? = null, removedAssistantIds: Set<String> = emptySet()) {
        uiState = uiState.copy(
            role = role,
            activeAssistantId = if (role == UserRole.ASSISTANT) assistantId else null,
            assistants = uiState.assistants.filterNot { it.id in removedAssistantIds }
        )
    }

    fun logout(removedAssistantIds: Set<String> = emptySet()) {
        val initial = DummyData.initialState()
        uiState = initial.copy(assistants = initial.assistants.filterNot { it.id in removedAssistantIds })
    }

    fun permissions(): Set<Permission> = when (uiState.role) {
        UserRole.DOCTOR -> Permission.entries.toSet()
        UserRole.ASSISTANT -> uiState.assistants.firstOrNull { it.id == uiState.activeAssistantId }?.permissions.orEmpty()
        null -> emptySet()
    }

    fun hasPermission(permission: Permission): Boolean = permission in permissions()

    fun toggleQueue() {
        if (!hasPermission(Permission.UPDATE_QUEUE)) return
        val next = when (uiState.queueState) {
            QueueState.ACTIVE -> QueueState.PAUSED
            QueueState.PAUSED, QueueState.NOT_STARTED -> QueueState.ACTIVE
            QueueState.CLOSED -> QueueState.CLOSED
        }
        uiState = uiState.copy(queueState = next)
    }

    fun callNext() {
        if (uiState.queueState != QueueState.ACTIVE || !hasPermission(Permission.CALL_NEXT_PATIENT)) return
        val next = uiState.appointments
            .filter { it.token > uiState.currentToken && it.status !in setOf(AppointmentStatus.ABSENT, AppointmentStatus.COMPLETED) }
            .minByOrNull { it.token } ?: return
        val updated = uiState.appointments.map { appointment ->
            when {
                appointment.token == uiState.currentToken && appointment.status == AppointmentStatus.IN_CONSULTATION -> appointment.copy(status = AppointmentStatus.COMPLETED)
                appointment.id == next.id -> appointment.copy(status = AppointmentStatus.IN_CONSULTATION)
                else -> appointment
            }
        }
        uiState = uiState.copy(appointments = updated, currentToken = next.token)
    }

    fun updateAppointment(id: String, status: AppointmentStatus) {
        val required = when (status) {
            AppointmentStatus.ARRIVED -> Permission.MARK_PATIENT_ARRIVED
            AppointmentStatus.ABSENT -> Permission.MARK_PATIENT_ABSENT
            AppointmentStatus.COMPLETED -> Permission.MARK_PATIENT_COMPLETED
            else -> Permission.UPDATE_QUEUE
        }
        if (!hasPermission(required)) return
        uiState = uiState.copy(appointments = uiState.appointments.map { if (it.id == id) it.copy(status = status) else it })
    }

    fun toggleAnnouncement(id: String) {
        if (uiState.role != UserRole.DOCTOR) return
        uiState = uiState.copy(announcements = uiState.announcements.map { if (it.id == id) it.copy(active = !it.active) else it })
    }

    fun toggleAppointments(blockId: String) {
        if (uiState.role != UserRole.DOCTOR) return
        uiState = uiState.copy(availabilityBlocks = uiState.availabilityBlocks.map {
            if (it.id == blockId) it.copy(appointmentsEnabled = !it.appointmentsEnabled) else it
        })
    }

    fun deleteAssistant(assistantId: String): Boolean {
        if (uiState.role != UserRole.DOCTOR || uiState.assistants.none { it.id == assistantId }) return false
        uiState = uiState.copy(assistants = uiState.assistants.filterNot { it.id == assistantId })
        return true
    }

    fun togglePermission(assistantId: String, permission: Permission) {
        if (uiState.role != UserRole.DOCTOR) return
        uiState = uiState.copy(assistants = uiState.assistants.map { assistant ->
            if (assistant.id != assistantId) assistant else {
                val permissions = assistant.permissions.toMutableSet()
                if (!permissions.add(permission)) permissions.remove(permission)
                assistant.copy(permissions = permissions)
            }
        })
    }
}