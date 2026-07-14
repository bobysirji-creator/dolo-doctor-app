package com.dolo.doctor.data

import com.dolo.doctor.data.model.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DoctorViewModelTest {
    @Test fun callNextCompletesCurrentAndAdvancesQueue() {
        val model = DoctorViewModel()
        model.callNext()
        assertEquals(10, model.uiState.currentToken)
        assertEquals(AppointmentStatus.COMPLETED, model.uiState.appointments.single { it.token == 9 }.status)
        assertEquals(AppointmentStatus.IN_CONSULTATION, model.uiState.appointments.single { it.token == 10 }.status)
    }

    @Test fun pausedQueueDoesNotAdvance() {
        val model = DoctorViewModel()
        model.toggleQueue()
        model.callNext()
        assertEquals(9, model.uiState.currentToken)
        assertEquals(QueueState.PAUSED, model.uiState.queueState)
    }

    @Test fun permissionsCanBeEnabledAndDisabled() {
        val model = DoctorViewModel()
        val assistant = model.uiState.assistants.first()
        assertFalse(Permission.MANAGE_ANNOUNCEMENTS in assistant.permissions)
        model.togglePermission(assistant.id, Permission.MANAGE_ANNOUNCEMENTS)
        assertTrue(Permission.MANAGE_ANNOUNCEMENTS in model.uiState.assistants.first().permissions)
        model.togglePermission(assistant.id, Permission.MANAGE_ANNOUNCEMENTS)
        assertFalse(Permission.MANAGE_ANNOUNCEMENTS in model.uiState.assistants.first().permissions)
    }
}