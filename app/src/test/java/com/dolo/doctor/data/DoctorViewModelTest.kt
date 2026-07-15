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
        assertEquals(AuditAction.DAY_CLOSED, model.uiState.auditEvents.last().action)

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
        assertEquals(AuditAction.DAY_ROLLED_OVER, model.uiState.auditEvents.single().action)
        assertEquals("System", model.uiState.auditEvents.single().actor)

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
                sessionQueues = listOf(
                    ConsultationQueue("Morning", QueueState.NOT_STARTED, 0),
                    ConsultationQueue("Evening", QueueState.NOT_STARTED, 0)
                ),
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
            sessionQueues = listOf(
                ConsultationQueue("Morning", QueueState.ACTIVE, 14),
                ConsultationQueue("Evening", QueueState.NOT_STARTED, 0)
            ),
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
        assertEquals(AuditAction.CONSULTATION_COMPLETED, model.uiState.auditEvents.last().action)

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
        assertTrue(model.updateClinic(original.copy(morningSession = "bad schedule")) != null)
        assertTrue(model.updateClinic(original.copy(eveningSession = "09:00 PM - 04:00 PM")) != null)
        assertEquals(original, model.uiState.clinics.first())

        model.login(UserRole.ASSISTANT, "staff-1")
        assertTrue(model.updateClinic(original.copy(name = "Assistant Edit Clinic")) != null)
        assertEquals(original, model.uiState.clinics.first())
    }
    @Test fun invalidAppointmentTransitionIsRejectedWithoutAudit() {
        val model = DoctorViewModel()
        model.login(UserRole.DOCTOR)

        model.updateAppointment("a4", AppointmentStatus.COMPLETED)

        assertEquals(AppointmentStatus.BOOKED, model.uiState.appointments.single { it.id == "a4" }.status)
        assertTrue(model.uiState.auditEvents.isEmpty())
    }

    @Test fun validStatusTransitionCreatesAttributedAuditEvent() {
        val model = DoctorViewModel(
            currentDate = { LocalDate.parse("2026-07-15") },
            currentTime = { LocalTime.of(9, 5) }
        )
        model.login(UserRole.DOCTOR)

        model.updateAppointment("a4", AppointmentStatus.ARRIVED)

        val event = model.uiState.auditEvents.first()
        assertEquals(AuditAction.STATUS_CHANGED, event.action)
        assertEquals("Dr. Aisha Mehta", event.actor)
        assertEquals(12, event.token)
        assertEquals("Aman Gupta", event.patientName)
        assertEquals(AppointmentStatus.BOOKED, event.fromStatus)
        assertEquals(AppointmentStatus.ARRIVED, event.toStatus)
        assertEquals("09:05 AM", event.time)
        assertEquals(AuditAction.RECEIPT_GENERATED, model.uiState.auditEvents.last().action)
    }

    @Test fun assistantQueueActionUsesAssistantIdentity() {
        val model = DoctorViewModel()
        model.login(UserRole.ASSISTANT, "staff-1")

        model.callNext()

        val event = model.uiState.auditEvents.last()
        assertEquals(AuditAction.PATIENT_CALLED, event.action)
        assertEquals("Neha Kapoor", event.actor)
        assertEquals(10, event.token)
    }

    @Test fun queueLifecycleAndAuditSurviveViewModelRecreation() {
        val store = MemoryDoctorStateStore()
        val first = DoctorViewModel(store)
        first.login(UserRole.DOCTOR)
        first.toggleQueue()
        first.toggleQueue()
        first.updateAppointment("a4", AppointmentStatus.ARRIVED)

        val restored = DoctorViewModel(store)

        assertEquals(
            listOf(AuditAction.QUEUE_PAUSED, AuditAction.QUEUE_RESUMED, AuditAction.STATUS_CHANGED, AuditAction.RECEIPT_GENERATED),
            restored.uiState.auditEvents.map { it.action }
        )
        assertEquals(AppointmentStatus.ARRIVED, restored.uiState.appointments.single { it.id == "a4" }.status)
    }

    @Test fun completedAppointmentIsTerminal() {
        val completed = DummyData.initialState().copy(
            appointments = DummyData.appointments.map { if (it.id == "a4") it.copy(status = AppointmentStatus.COMPLETED) else it }
        )
        val model = DoctorViewModel(MemoryDoctorStateStore(completed))
        model.login(UserRole.DOCTOR)

        model.updateAppointment("a4", AppointmentStatus.WAITING)

        assertEquals(AppointmentStatus.COMPLETED, model.uiState.appointments.single { it.id == "a4" }.status)
        assertTrue(model.uiState.auditEvents.isEmpty())
    }
    @Test fun accidentalSkipCanResumeImmediatelyWhenNoOtherConsultationIsActive() {
        val skipped = DummyData.initialState().copy(
            appointments = DummyData.appointments.map {
                when (it.id) {
                    "a2" -> it.copy(status = AppointmentStatus.SKIPPED)
                    else -> it.copy(status = AppointmentStatus.COMPLETED)
                }
            }
        )
        val model = DoctorViewModel(MemoryDoctorStateStore(skipped))
        model.login(UserRole.DOCTOR)

        assertTrue(model.resumeSkippedConsultation("a2"))
        assertEquals(10, model.uiState.currentToken)
        assertEquals(AppointmentStatus.IN_CONSULTATION, model.uiState.appointments.single { it.id == "a2" }.status)
    }

    @Test fun skippedPatientCannotResumeNowWhileAnotherConsultationIsActive() {
        val active = DummyData.initialState().copy(
            appointments = DummyData.appointments.map { if (it.id == "a2") it.copy(status = AppointmentStatus.SKIPPED) else it }
        )
        val model = DoctorViewModel(MemoryDoctorStateStore(active))
        model.login(UserRole.DOCTOR)

        assertFalse(model.resumeSkippedConsultation("a2"))
        assertEquals(AppointmentStatus.SKIPPED, model.uiState.appointments.single { it.id == "a2" }.status)
    }
    @Test fun skippedPatientCanRejoinAtEndAndReturnToConsultation() {
        val finalQueue = DummyData.initialState().copy(
            currentToken = 14,
            sessionQueues = listOf(
                ConsultationQueue("Morning", QueueState.ACTIVE, 14),
                ConsultationQueue("Evening", QueueState.NOT_STARTED, 0)
            ),
            appointments = DummyData.appointments.map {
                when (it.id) {
                    "a2" -> it.copy(status = AppointmentStatus.SKIPPED)
                    else -> it.copy(status = AppointmentStatus.COMPLETED)
                }
            }
        )
        val model = DoctorViewModel(MemoryDoctorStateStore(finalQueue))
        model.login(UserRole.DOCTOR)

        model.updateAppointment("a2", AppointmentStatus.WAITING)
        assertEquals(AppointmentStatus.SKIPPED, model.uiState.appointments.single { it.id == "a2" }.status)

        assertTrue(model.rejoinAppointment("a2"))
        val rejoined = model.uiState.appointments.single { it.id == "a2" }
        assertEquals(AppointmentStatus.WAITING, rejoined.status)
        assertTrue(rejoined.queueOrder > 14)

        model.callNext()
        assertEquals(10, model.uiState.currentToken)
        assertEquals(AppointmentStatus.IN_CONSULTATION, model.uiState.appointments.single { it.id == "a2" }.status)
        assertTrue(model.uiState.auditEvents.any { it.action == AuditAction.PATIENT_REJOINED })
    }

    @Test fun lateArrivalKeepsTokenButJoinsAtEndAndGetsReceipt() {
        val progressed = DummyData.initialState("2026-07-15").copy(
            currentToken = 14,
            sessionQueues = listOf(
                ConsultationQueue("Morning", QueueState.ACTIVE, 14),
                ConsultationQueue("Evening", QueueState.NOT_STARTED, 0)
            ),
            appointments = DummyData.appointments.map {
                when (it.id) {
                    "a4" -> it.copy(status = AppointmentStatus.BOOKED, receiptNumber = "")
                    else -> it.copy(status = AppointmentStatus.COMPLETED)
                }
            }
        )
        val model = DoctorViewModel(
            MemoryDoctorStateStore(progressed),
            currentDate = { LocalDate.parse("2026-07-15") },
            currentTime = { LocalTime.of(11, 0) }
        )
        model.login(UserRole.DOCTOR)

        model.updateAppointment("a4", AppointmentStatus.ARRIVED)

        val late = model.uiState.appointments.single { it.id == "a4" }
        assertEquals(12, late.token)
        assertEquals(AppointmentStatus.ARRIVED, late.status)
        assertTrue(late.queueOrder > 14)
        assertEquals("DL-20260715-012", late.receiptNumber)
        assertEquals(listOf(AuditAction.PATIENT_REJOINED, AuditAction.RECEIPT_GENERATED), model.uiState.auditEvents.map { it.action })
    }

    @Test fun assistantCanBookWalkInAndGenerateCompulsoryReceipt() {
        val store = MemoryDoctorStateStore()
        val model = DoctorViewModel(
            store,
            currentDate = { LocalDate.parse("2026-07-15") },
            currentTime = { LocalTime.of(9, 30) }
        )
        model.login(UserRole.ASSISTANT, "staff-1")

        val result = model.bookWalkIn(WalkInBookingRequest("Sohan Lal", "9876512345", "Self", "Morning"))

        assertEquals(null, result.error)
        val receipt = result.receipt ?: throw AssertionError("Receipt was not generated")
        assertEquals(15, receipt.token)
        assertEquals("DL-20260715-015", receipt.receiptNumber)
        val appointment = model.uiState.appointments.single { it.token == 15 }
        assertEquals(BookingSource.CLINIC_WALK_IN, appointment.bookingSource)
        assertEquals(AppointmentStatus.ARRIVED, appointment.status)
        assertEquals("9876512345", appointment.patientPhone)
        assertEquals(listOf(AuditAction.WALK_IN_BOOKED, AuditAction.RECEIPT_GENERATED), model.uiState.auditEvents.map { it.action })

        val restored = DoctorViewModel(store, currentDate = { LocalDate.parse("2026-07-15") })
        assertEquals("DL-20260715-015", restored.uiState.appointments.single { it.token == 15 }.receiptNumber)
    }

    @Test fun viewOnlyAssistantCannotBookWalkInOrGenerateReceipt() {
        val model = DoctorViewModel()
        model.login(UserRole.ASSISTANT, "staff-2")

        val result = model.bookWalkIn(WalkInBookingRequest("Sohan Lal", "9876512345", "Self", "Morning"))

        assertTrue(result.error != null)
        assertEquals(6, model.uiState.appointments.size)
        assertEquals(null, model.receiptFor("a4"))
        assertTrue(model.uiState.auditEvents.isEmpty())
    }
    @Test fun morningAndEveningQueuesAdvanceIndependently() {
        val eveningAppointment = Appointment(
            id = "evening-1",
            token = 15,
            patientName = "Evening Patient",
            patientType = "Self",
            session = "Evening",
            status = AppointmentStatus.WAITING,
            bookedAt = "04:30 PM",
            queueOrder = 1
        )
        val initial = DummyData.initialState().copy(
            appointments = DummyData.appointments + eveningAppointment,
            sessionQueues = listOf(
                ConsultationQueue("Morning", QueueState.ACTIVE, 9),
                ConsultationQueue("Evening", QueueState.ACTIVE, 0)
            )
        )
        val model = DoctorViewModel(MemoryDoctorStateStore(initial))
        model.login(UserRole.DOCTOR)

        model.callNext("Evening")

        assertEquals(9, model.queueFor("Morning").currentToken)
        assertEquals(15, model.queueFor("Evening").currentToken)
        assertEquals(AppointmentStatus.IN_CONSULTATION, model.uiState.appointments.single { it.id == "evening-1" }.status)
        assertEquals(AppointmentStatus.IN_CONSULTATION, model.uiState.appointments.single { it.id == "a1" }.status)

        model.toggleQueue("Morning")
        assertEquals(QueueState.PAUSED, model.queueFor("Morning").state)
        assertEquals(QueueState.ACTIVE, model.queueFor("Evening").state)
    }

    @Test fun sessionsAllowAdvanceBookingButCloseAtTheirOwnEndTime() {
        val beforeMorningEnd = DoctorViewModel(
            currentDate = { LocalDate.parse("2026-07-15") },
            currentTime = { LocalTime.of(12, 59) }
        )
        beforeMorningEnd.login(UserRole.DOCTOR)
        assertTrue(beforeMorningEnd.sessionBookingOpen("Morning"))
        assertTrue(beforeMorningEnd.sessionBookingOpen("Evening"))

        val atMorningEnd = DoctorViewModel(
            currentDate = { LocalDate.parse("2026-07-15") },
            currentTime = { LocalTime.of(13, 0) }
        )
        atMorningEnd.login(UserRole.DOCTOR)
        assertFalse(atMorningEnd.sessionBookingOpen("Morning"))
        assertTrue(atMorningEnd.sessionBookingOpen("Evening"))
        assertTrue(atMorningEnd.bookWalkIn(WalkInBookingRequest("Morning Closed", "9876512345", "Self", "Morning")).error != null)
        assertEquals(null, atMorningEnd.bookWalkIn(WalkInBookingRequest("Evening Advance", "9876512345", "Self", "Evening")).error)

        val atEveningEnd = DoctorViewModel(
            currentDate = { LocalDate.parse("2026-07-15") },
            currentTime = { LocalTime.of(21, 0) }
        )
        atEveningEnd.login(UserRole.DOCTOR)
        assertFalse(atEveningEnd.sessionBookingOpen("Morning"))
        assertFalse(atEveningEnd.sessionBookingOpen("Evening"))
    }

    @Test fun nextDayRolloverResetsBothSessionsAndReopensAdvanceBooking() {
        val previousDay = DummyData.initialState("2026-07-14").copy(
            sessionQueues = listOf(
                ConsultationQueue("Morning", QueueState.CLOSED, 14),
                ConsultationQueue("Evening", QueueState.CLOSED, 22)
            ),
            queueState = QueueState.CLOSED,
            currentToken = 14
        )
        val model = DoctorViewModel(
            stateStore = MemoryDoctorStateStore(previousDay),
            currentDate = { LocalDate.parse("2026-07-15") },
            currentTime = { LocalTime.of(8, 0) }
        )

        assertEquals(QueueState.NOT_STARTED, model.queueFor("Morning").state)
        assertEquals(QueueState.NOT_STARTED, model.queueFor("Evening").state)
        assertEquals(0, model.queueFor("Morning").currentToken)
        assertEquals(0, model.queueFor("Evening").currentToken)
        assertTrue(model.sessionBookingOpen("Morning"))
        assertTrue(model.sessionBookingOpen("Evening"))
    }

    @Test fun independentSessionQueuesSurviveViewModelRecreation() {
        val store = MemoryDoctorStateStore()
        val first = DoctorViewModel(store)
        first.login(UserRole.DOCTOR)
        first.toggleQueue("Evening")

        val restored = DoctorViewModel(store)

        assertEquals(QueueState.ACTIVE, restored.queueFor("Morning").state)
        assertEquals(QueueState.ACTIVE, restored.queueFor("Evening").state)
        assertEquals(9, restored.queueFor("Morning").currentToken)
        assertEquals(0, restored.queueFor("Evening").currentToken)
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
