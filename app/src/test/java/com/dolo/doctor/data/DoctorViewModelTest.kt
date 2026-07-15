package com.dolo.doctor.data

import com.dolo.doctor.data.model.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class DoctorViewModelTest {
    @Test fun doctorCanCallNext() {
        val model = DoctorViewModel()
        model.login(UserRole.DOCTOR)
        model.callNext()
        assertEquals(10, model.uiState.currentToken)
        assertEquals(AppointmentStatus.COMPLETED, model.uiState.appointments.single { it.token == 9 }.status)
        assertEquals(AppointmentStatus.IN_CONSULTATION, model.uiState.appointments.single { it.token == 10 }.status)
    }

    @Test fun workflowStateSurvivesViewModelRecreation() {
        val store = MemoryDoctorStateStore()
        val first = DoctorViewModel(store)
        first.login(UserRole.DOCTOR)
        first.callNext()
        first.toggleAnnouncement("n1")

        val restored = DoctorViewModel(store)
        restored.login(UserRole.DOCTOR)
        assertEquals(10, restored.uiState.currentToken)
        assertEquals(AppointmentStatus.COMPLETED, restored.uiState.appointments.single { it.token == 9 }.status)
        assertEquals(AppointmentStatus.IN_CONSULTATION, restored.uiState.appointments.single { it.token == 10 }.status)
        assertFalse(restored.uiState.announcements.single { it.id == "n1" }.active)
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

    @Test fun loginRoleChangeKeepsCurrentWorkflowState() {
        val model = DoctorViewModel()
        model.login(UserRole.DOCTOR)
        model.callNext()

        model.login(UserRole.ASSISTANT, "staff-1")

        assertEquals(10, model.uiState.currentToken)
        assertEquals(AppointmentStatus.IN_CONSULTATION, model.uiState.appointments.single { it.token == 10 }.status)
    }

    @Test fun logoutClearsRoleWithoutResettingWorkflowState() {
        val model = DoctorViewModel()
        model.login(UserRole.DOCTOR)
        model.callNext()

        model.logout()

        assertEquals(null, model.uiState.role)
        assertEquals(null, model.uiState.activeAssistantId)
        assertEquals(10, model.uiState.currentToken)
        assertEquals(AppointmentStatus.IN_CONSULTATION, model.uiState.appointments.single { it.token == 10 }.status)
    }

    @Test fun doctorCanCloseAndArchiveTheCurrentDay() {
        val model = DoctorViewModel(
            currentDate = { LocalDate.parse("2026-07-15") },
            currentTime = { LocalTime.of(21, 30) }
        )
        model.login(UserRole.DOCTOR)
        model.callNext()

        assertTrue(model.closeDay())
        assertEquals(QueueState.CLOSED, model.uiState.queueState)
        assertEquals(1, model.uiState.queueHistory.size)
        val history = model.uiState.queueHistory.single()
        assertEquals("2026-07-15", history.date)
        assertEquals("09:30 PM", history.closedAt)
        assertEquals(10, history.finalToken)
        assertEquals(model.uiState.appointments, history.appointments)

        model.callNext()
        model.updateAppointment("a3", AppointmentStatus.ABSENT)
        assertEquals(10, model.uiState.currentToken)
        assertEquals(AppointmentStatus.ARRIVED, model.uiState.appointments.single { it.id == "a3" }.status)
    }

    @Test fun assistantCannotCloseTheDay() {
        val model = DoctorViewModel()
        model.login(UserRole.ASSISTANT, "staff-1")

        assertFalse(model.closeDay())
        assertEquals(QueueState.ACTIVE, model.uiState.queueState)
        assertTrue(model.uiState.queueHistory.isEmpty())
    }

    @Test fun nextDateArchivesOldQueueAndStartsEmptyDay() {
        val store = MemoryDoctorStateStore(DummyData.initialState("2026-07-14"))
        val model = DoctorViewModel(
            stateStore = store,
            currentDate = { LocalDate.parse("2026-07-15") },
            currentTime = { LocalTime.of(7, 0) }
        )

        assertEquals("2026-07-15", model.uiState.queueDate)
        assertEquals(QueueState.NOT_STARTED, model.uiState.queueState)
        assertEquals(0, model.uiState.currentToken)
        assertTrue(model.uiState.appointments.isEmpty())
        assertEquals("2026-07-14", model.uiState.queueHistory.single().date)
        assertEquals("Automatic date rollover", model.uiState.queueHistory.single().closureReason)

        val restored = DoctorViewModel(
            stateStore = store,
            currentDate = { LocalDate.parse("2026-07-15") },
            currentTime = { LocalTime.of(7, 1) }
        )
        assertTrue(restored.uiState.appointments.isEmpty())
        assertEquals(1, restored.uiState.queueHistory.size)
    }

    @Test fun doctorCanArchiveAnEmptyDay() {
        val store = MemoryDoctorStateStore(
            DummyData.initialState("2026-07-15").copy(
                appointments = emptyList(),
                queueState = QueueState.NOT_STARTED,
                currentToken = 0
            )
        )
        val model = DoctorViewModel(
            stateStore = store,
            currentDate = { LocalDate.parse("2026-07-15") },
            currentTime = { LocalTime.of(18, 0) }
        )
        model.login(UserRole.DOCTOR)

        assertTrue(model.closeDay())
        assertEquals(QueueState.CLOSED, model.uiState.queueState)
        assertTrue(model.uiState.queueHistory.single().appointments.isEmpty())
        assertEquals(0, model.uiState.queueHistory.single().finalToken)
    }

    @Test fun archivedHistorySurvivesViewModelRecreation() {
        val store = MemoryDoctorStateStore()
        val first = DoctorViewModel(
            stateStore = store,
            currentDate = { LocalDate.parse("2026-07-15") },
            currentTime = { LocalTime.of(20, 45) }
        )
        first.login(UserRole.DOCTOR)
        first.closeDay()

        val restored = DoctorViewModel(
            stateStore = store,
            currentDate = { LocalDate.parse("2026-07-15") },
            currentTime = { LocalTime.of(20, 46) }
        )

        assertEquals(QueueState.CLOSED, restored.uiState.queueState)
        assertEquals(1, restored.uiState.queueHistory.size)
        assertEquals(first.uiState.appointments, restored.uiState.queueHistory.single().appointments)
    }

    @Test fun callNextCompletesTheFinalConsultationBeforeArchive() {
        val finalState = DummyData.initialState("2026-07-15").copy(
            currentToken = 14,
            appointments = DummyData.appointments.map {
                if (it.token == 14) it.copy(status = AppointmentStatus.IN_CONSULTATION)
                else it.copy(status = AppointmentStatus.COMPLETED)
            }
        )
        val model = DoctorViewModel(
            stateStore = MemoryDoctorStateStore(finalState),
            currentDate = { LocalDate.parse("2026-07-15") },
            currentTime = { LocalTime.of(21, 0) }
        )
        model.login(UserRole.DOCTOR)

        model.callNext()
        assertEquals(AppointmentStatus.COMPLETED, model.uiState.appointments.single { it.token == 14 }.status)

        assertTrue(model.closeDay())
        assertEquals(AppointmentStatus.COMPLETED, model.uiState.queueHistory.single().appointments.single { it.token == 14 }.status)
    }

    @Test fun validatedProfileChangesPersistAndSensitiveFieldsNeedReview() {
        val store = MemoryDoctorStateStore()
        val model = DoctorViewModel(store)
        model.login(UserRole.DOCTOR)

        assertEquals(null, model.updateProfile(model.uiState.profile.copy(name = "Dr. Aisha M. Mehta", consultationFee = 650)))
        assertEquals(ProfileReviewStatus.VERIFIED, model.uiState.profile.reviewStatus)

        assertEquals(null, model.updateProfile(model.uiState.profile.copy(specialty = "Internal Medicine")))
        assertEquals(ProfileReviewStatus.PENDING_REVIEW, model.uiState.profile.reviewStatus)

        val restored = DoctorViewModel(store)
        assertEquals("Dr. Aisha M. Mehta", restored.uiState.profile.name)
        assertEquals(650, restored.uiState.profile.consultationFee)
        assertEquals("Internal Medicine", restored.uiState.profile.specialty)
        assertEquals(ProfileReviewStatus.PENDING_REVIEW, restored.uiState.profile.reviewStatus)
    }

    @Test fun invalidOrAssistantProfileChangesAreRejected() {
        val model = DoctorViewModel()
        model.login(UserRole.DOCTOR)
        val original = model.uiState.profile
        assertTrue(model.updateProfile(original.copy(name = "A")) != null)
        assertEquals(original, model.uiState.profile)

        model.login(UserRole.ASSISTANT, "staff-1")
        assertTrue(model.updateProfile(original.copy(name = "Dr. Changed Name")) != null)
        assertEquals(original, model.uiState.profile)
    }

    @Test fun validatedClinicAndScheduleChangesPersist() {
        val store = MemoryDoctorStateStore()
        val model = DoctorViewModel(store)
        model.login(UserRole.DOCTOR)
        val updated = model.uiState.clinics.first().copy(
            phone = "+91 11 4444 5555",
            morningSession = "08:30 AM - 12:30 PM",
            maxTokensPerSession = 40,
            averageConsultationMinutes = 15
        )

        assertEquals(null, model.updateClinic(updated))
        val restored = DoctorViewModel(store)
        assertEquals(updated, restored.uiState.clinics.first())
    }

    @Test fun invalidClinicScheduleIsRejected() {
        val model = DoctorViewModel()
        model.login(UserRole.DOCTOR)
        val original = model.uiState.clinics.first()

        assertTrue(model.updateClinic(original.copy(maxTokensPerSession = 0, averageConsultationMinutes = 2)) != null)
        assertEquals(original, model.uiState.clinics.first())

        model.login(UserRole.ASSISTANT, "staff-1")
        assertTrue(model.updateClinic(original.copy(name = "Assistant Edit Clinic")) != null)
        assertEquals(original, model.uiState.clinics.first())
    }
    private class MemoryDoctorStateStore(initial: DoctorUiState? = null) : DoctorStateStore {
        private var saved: DoctorUiState? = initial
        override fun restore(defaultState: DoctorUiState): DoctorUiState = saved ?: defaultState
        override fun save(state: DoctorUiState): Boolean {
            saved = state
            return true
        }
    }
}