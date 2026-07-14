package com.dolo.doctor.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.dolo.doctor.data.model.*

class DoctorViewModel : ViewModel() {
    var uiState by mutableStateOf(DummyData.initialState())
        private set

    fun login(role: UserRole) { uiState = uiState.copy(role = role) }
    fun logout() { uiState = DummyData.initialState() }

    fun toggleQueue() {
        val next = when (uiState.queueState) {
            QueueState.ACTIVE -> QueueState.PAUSED
            QueueState.PAUSED, QueueState.NOT_STARTED -> QueueState.ACTIVE
            QueueState.CLOSED -> QueueState.CLOSED
        }
        uiState = uiState.copy(queueState = next)
    }

    fun callNext() {
        if (uiState.queueState != QueueState.ACTIVE) return
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
        uiState = uiState.copy(appointments = uiState.appointments.map { if (it.id == id) it.copy(status = status) else it })
    }

    fun toggleAnnouncement(id: String) {
        uiState = uiState.copy(announcements = uiState.announcements.map { if (it.id == id) it.copy(active = !it.active) else it })
    }

    fun toggleAppointments(blockId: String) {
        uiState = uiState.copy(availabilityBlocks = uiState.availabilityBlocks.map {
            if (it.id == blockId) it.copy(appointmentsEnabled = !it.appointmentsEnabled) else it
        })
    }

    fun togglePermission(assistantId: String, permission: Permission) {
        uiState = uiState.copy(assistants = uiState.assistants.map { assistant ->
            if (assistant.id != assistantId) assistant else {
                val permissions = assistant.permissions.toMutableSet()
                if (!permissions.add(permission)) permissions.remove(permission)
                assistant.copy(permissions = permissions)
            }
        })
    }
}