package com.dolo.doctor.data

import com.dolo.doctor.data.model.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DoctorViewModelTest {
    @Test fun doctorCanCallNext() {
        val model = DoctorViewModel()
        model.login(UserRole.DOCTOR)
        model.callNext()
        assertEquals(10, model.uiState.currentToken)
        assertEquals(AppointmentStatus.COMPLETED, model.uiState.appointments.single { it.token == 9 }.status)
        assertEquals(AppointmentStatus.IN_CONSULTATION, model.uiState.appointments.single { it.token == 10 }.status)
    }

    @Test fun pausedQueueDoesNotAdvance() {
        val model = DoctorViewModel()
        model.login(UserRole.DOCTOR)
        model.toggleQueue()
        model.callNext()
        assertEquals(9, model.uiState.currentToken)
        assertEquals(QueueState.PAUSED, model.uiState.queueState)
    }

    @Test fun viewOnlyAssistantCannotChangeQueue() {
        val model = DoctorViewModel()
        model.login(UserRole.ASSISTANT, "staff-2")
        assertTrue(model.hasPermission(Permission.VIEW_QUEUE))
        assertFalse(model.hasPermission(Permission.CALL_NEXT_PATIENT))
        model.callNext()
        model.toggleQueue()
        assertEquals(9, model.uiState.currentToken)
        assertEquals(QueueState.ACTIVE, model.uiState.queueState)
    }

    @Test fun permittedAssistantCanCallNextButCannotMarkAbsent() {
        val model = DoctorViewModel()
        model.login(UserRole.ASSISTANT, "staff-1")
        model.callNext()
        assertEquals(10, model.uiState.currentToken)
        model.updateAppointment("a3", AppointmentStatus.ABSENT)
        assertEquals(AppointmentStatus.ARRIVED, model.uiState.appointments.single { it.id == "a3" }.status)
    }

    @Test fun onlyDoctorCanDeleteAssistant() {
        val model = DoctorViewModel()
        model.login(UserRole.ASSISTANT, "staff-1")
        assertFalse(model.deleteAssistant("staff-2"))
        assertEquals(2, model.uiState.assistants.size)

        model.login(UserRole.DOCTOR)
        assertTrue(model.deleteAssistant("staff-2"))
        assertEquals(listOf("staff-1"), model.uiState.assistants.map { it.id })
    }

    @Test fun removedAssistantsStayFilteredWhenStateIsRestored() {
        val model = DoctorViewModel()
        model.login(UserRole.DOCTOR, removedAssistantIds = setOf("staff-2"))
        assertEquals(listOf("staff-1"), model.uiState.assistants.map { it.id })
        model.logout(setOf("staff-2"))
        assertEquals(listOf("staff-1"), model.uiState.assistants.map { it.id })
    }

    @Test fun onlyDoctorCanChangeAssistantPermissions() {
        val model = DoctorViewModel()
        model.login(UserRole.DOCTOR)
        val assistant = model.uiState.assistants.first()
        assertFalse(Permission.MANAGE_ANNOUNCEMENTS in assistant.permissions)
        model.togglePermission(assistant.id, Permission.MANAGE_ANNOUNCEMENTS)
        assertTrue(Permission.MANAGE_ANNOUNCEMENTS in model.uiState.assistants.first().permissions)
        model.login(UserRole.ASSISTANT, assistant.id)
        model.togglePermission(assistant.id, Permission.MANAGE_ANNOUNCEMENTS)
        assertTrue(Permission.MANAGE_ANNOUNCEMENTS in model.uiState.assistants.first().permissions)
    }
}