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
        assertEquals(2, model.uiState.currentToken)
        assertEquals(AppointmentStatus.COMPLETED, model.uiState.appointments.single { it.token == 1 }.status)
        assertEquals(AppointmentStatus.IN_CONSULTATION, model.uiState.appointments.single { it.token == 2 }.status)
    }

    @Test fun workflowStateSurvivesViewModelRecreation() {
        val store = MemoryDoctorStateStore()
        val first = DoctorViewModel(store)
        first.login(UserRole.DOCTOR)
        first.callNext()
        first.toggleAnnouncement("n1")

        val restored = DoctorViewModel(store)
        restored.login(UserRole.DOCTOR)
        assertEquals(2, restored.uiState.currentToken)
        assertEquals(AppointmentStatus.COMPLETED, restored.uiState.appointments.single { it.token == 1 }.status)
        assertEquals(AppointmentStatus.IN_CONSULTATION, restored.uiState.appointments.single { it.token == 2 }.status)
        assertFalse(restored.uiState.announcements.single { it.id == "n1" }.active)
    }

    @Test fun pausedQueueDoesNotAdvance() {
        val model = DoctorViewModel()
        model.login(UserRole.DOCTOR)
        model.toggleQueue()
        model.callNext()
        assertEquals(1, model.uiState.currentToken)
        assertEquals(QueueState.PAUSED, model.uiState.queueState)
    }

    @Test fun viewOnlyAssistantCannotChangeQueue() {
        val model = DoctorViewModel()
        model.login(UserRole.ASSISTANT, "staff-2")
        assertTrue(model.hasPermission(Permission.VIEW_QUEUE))
        assertFalse(model.hasPermission(Permission.CALL_NEXT_PATIENT))
        model.callNext()
        model.toggleQueue()
        assertEquals(1, model.uiState.currentToken)
        assertEquals(QueueState.ACTIVE, model.uiState.queueState)
    }

    @Test fun permittedAssistantCanCallNextButCannotMarkAbsent() {
        val model = DoctorViewModel()
        model.login(UserRole.ASSISTANT, "staff-1")
        model.callNext()
        assertEquals(2, model.uiState.currentToken)
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

        assertEquals(2, model.uiState.currentToken)
        assertEquals(AppointmentStatus.IN_CONSULTATION, model.uiState.appointments.single { it.token == 2 }.status)
    }

    @Test fun logoutClearsRoleWithoutResettingWorkflowState() {
        val model = DoctorViewModel()
        model.login(UserRole.DOCTOR)
        model.callNext()

        model.logout()

        assertEquals(null, model.uiState.role)
        assertEquals(null, model.uiState.activeAssistantId)
        assertEquals(2, model.uiState.currentToken)
        assertEquals(AppointmentStatus.IN_CONSULTATION, model.uiState.appointments.single { it.token == 2 }.status)
    }

    @Test fun doctorCanCloseAndArchiveTheCurrentDay() {
        val model = DoctorViewModel(
            currentDate = { LocalDate.parse("2026-07-15") },
            currentTime = { LocalTime.of(21, 30) }
        )
        model.login(UserRole.DOCTOR)
        model.callNext()

        assertTrue(model.closeSession("Morning"))
        assertEquals(QueueState.CLOSED, model.queueFor("Morning").state)
        assertEquals(QueueState.NOT_STARTED, model.queueFor("Evening").state)
        assertTrue(model.uiState.queueHistory.isEmpty())
        assertTrue(model.closeSession("Evening"))
        assertEquals(QueueState.CLOSED, model.uiState.queueState)
        assertEquals(1, model.uiState.queueHistory.size)
        val history = model.uiState.queueHistory.single()
        assertEquals("2026-07-15", history.date)
        assertEquals("09:30 PM", history.closedAt)
        assertEquals(2, history.finalToken)
        assertEquals(model.uiState.appointments, history.appointments)
        assertEquals(AuditAction.DAY_CLOSED, model.uiState.auditEvents.last().action)

        model.callNext()
        model.updateAppointment("a3", AppointmentStatus.ABSENT)
        assertEquals(2, model.uiState.currentToken)
        assertEquals(AppointmentStatus.ARRIVED, model.uiState.appointments.single { it.id == "a3" }.status)
    }

    @Test fun assistantCannotCloseTheDay() {
        val model = DoctorViewModel()
        model.login(UserRole.ASSISTANT, "staff-1")

        assertFalse(model.closeSession("Morning"))
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

        assertTrue(model.closeSession("Morning"))
        assertTrue(model.closeSession("Evening"))
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
        first.closeSession("Morning")
        first.closeSession("Evening")

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
            currentToken = 3,
            sessionQueues = listOf(
                ConsultationQueue("Morning", QueueState.ACTIVE, 3),
                ConsultationQueue("Evening", QueueState.NOT_STARTED, 0)
            ),
            appointments = DummyData.appointments.map {
                when (it.id) {
                    "a1", "a2" -> it.copy(status = AppointmentStatus.COMPLETED)
                    "a3" -> it.copy(status = AppointmentStatus.IN_CONSULTATION)
                    else -> it
                }
            }
        )
        val model = DoctorViewModel(MemoryDoctorStateStore(finalState), currentDate = { LocalDate.parse("2026-07-15") }, currentTime = { LocalTime.of(21, 0) })
        model.login(UserRole.DOCTOR)

        model.callNext()
        assertEquals(AppointmentStatus.COMPLETED, model.uiState.appointments.single { it.id == "a3" }.status)
        assertEquals(AuditAction.CONSULTATION_COMPLETED, model.uiState.auditEvents.last().action)
        assertTrue(model.closeSession("Morning"))
        assertTrue(model.closeSession("Evening"))
        assertEquals(AppointmentStatus.COMPLETED, model.uiState.queueHistory.single().appointments.single { it.id == "a3" }.status)
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

    @Test fun feeConfirmationCreatesReceiptAndAttributedAuditEvents() {
        val model = DoctorViewModel(currentDate = { LocalDate.parse("2026-07-15") }, currentTime = { LocalTime.of(9, 5) })
        model.login(UserRole.DOCTOR)

        val result = model.confirmConsultationFee("a4", 500, PaymentMethod.UPI)

        assertEquals(null, result.error)
        val appointment = model.uiState.appointments.single { it.id == "a4" }
        assertEquals(AppointmentStatus.ARRIVED, appointment.status)
        assertEquals(PaymentStatus.PAID, appointment.paymentStatus)
        assertEquals(PaymentMethod.UPI, appointment.paymentMethod)
        assertEquals(500, appointment.consultationFee)
        assertEquals("DL-20260715-M-004", appointment.receiptNumber)
        assertEquals(listOf(AuditAction.FEE_CONFIRMED, AuditAction.RECEIPT_GENERATED), model.uiState.auditEvents.map { it.action })
        assertEquals("Dr. Aisha Mehta", model.uiState.auditEvents.first().actor)
        assertEquals("09:05 AM", model.uiState.auditEvents.first().time)
    }
    @Test fun unpaidAppointmentsAreNotCalledUntilFeeIsConfirmed() {
        val model = DoctorViewModel(currentDate = { LocalDate.parse("2026-07-15") }, currentTime = { LocalTime.of(10, 0) })
        model.login(UserRole.DOCTOR)

        model.callNext()
        model.callNext()
        model.callNext()

        assertEquals(3, model.queueFor("Morning").currentToken)
        assertEquals(AppointmentStatus.BOOKED, model.uiState.appointments.single { it.id == "a4" }.status)
        model.confirmConsultationFee("a4", 500, PaymentMethod.CASH)
        model.callNext()
        assertEquals(4, model.queueFor("Morning").currentToken)
        assertEquals(AppointmentStatus.IN_CONSULTATION, model.uiState.appointments.single { it.id == "a4" }.status)
    }
    @Test fun assistantQueueActionUsesAssistantIdentity() {
        val model = DoctorViewModel()
        model.login(UserRole.ASSISTANT, "staff-1")

        model.callNext()

        val event = model.uiState.auditEvents.last()
        assertEquals(AuditAction.PATIENT_CALLED, event.action)
        assertEquals("Neha Kapoor", event.actor)
        assertEquals(2, event.token)
    }

    @Test fun queueLifecycleFeeAdmissionAndAuditSurviveViewModelRecreation() {
        val store = MemoryDoctorStateStore()
        val first = DoctorViewModel(store)
        first.login(UserRole.DOCTOR)
        first.toggleQueue()
        first.toggleQueue()
        first.confirmConsultationFee("a4", 500, PaymentMethod.CASH)

        val restored = DoctorViewModel(store)

        assertEquals(
            listOf(AuditAction.QUEUE_PAUSED, AuditAction.QUEUE_RESUMED, AuditAction.FEE_CONFIRMED, AuditAction.RECEIPT_GENERATED),
            restored.uiState.auditEvents.map { it.action }
        )
        val appointment = restored.uiState.appointments.single { it.id == "a4" }
        assertEquals(AppointmentStatus.ARRIVED, appointment.status)
        assertEquals(PaymentStatus.PAID, appointment.paymentStatus)
    }
    @Test fun completedAppointmentIsTerminal() {
        val completed = DummyData.initialState().copy(
            appointments = DummyData.appointments.map { if (it.id == "a3") it.copy(status = AppointmentStatus.COMPLETED) else it }
        )
        val model = DoctorViewModel(MemoryDoctorStateStore(completed))
        model.login(UserRole.DOCTOR)

        model.updateAppointment("a3", AppointmentStatus.WAITING)

        assertEquals(AppointmentStatus.COMPLETED, model.uiState.appointments.single { it.id == "a3" }.status)
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
        assertEquals(2, model.uiState.currentToken)
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
            currentToken = 3,
            sessionQueues = listOf(ConsultationQueue("Morning", QueueState.ACTIVE, 3), ConsultationQueue("Evening", QueueState.NOT_STARTED, 0)),
            appointments = DummyData.appointments.map {
                when (it.id) {
                    "a2" -> it.copy(status = AppointmentStatus.SKIPPED)
                    "a1", "a3" -> it.copy(status = AppointmentStatus.COMPLETED)
                    else -> it
                }
            }
        )
        val model = DoctorViewModel(MemoryDoctorStateStore(finalQueue))
        model.login(UserRole.DOCTOR)

        assertTrue(model.rejoinAppointment("a2"))
        val rejoined = model.uiState.appointments.single { it.id == "a2" }
        assertEquals(AppointmentStatus.WAITING, rejoined.status)
        assertTrue(rejoined.queueOrder > 3)
        model.callNext()
        assertEquals(2, model.uiState.currentToken)
        assertEquals(AppointmentStatus.IN_CONSULTATION, model.uiState.appointments.single { it.id == "a2" }.status)
    }
    @Test fun unpaidOnlineAppointmentStaysOutUntilFeeConfirmation() {
        val store = MemoryDoctorStateStore()
        val model = DoctorViewModel(store, currentDate = { LocalDate.parse("2026-07-15") }, currentTime = { LocalTime.of(11, 0) })
        model.login(UserRole.DOCTOR)

        assertEquals(null, model.receiptFor("a4"))
        assertEquals(PaymentStatus.PENDING, model.uiState.appointments.single { it.id == "a4" }.paymentStatus)
        val result = model.confirmConsultationFee("a4", 450, PaymentMethod.UPI)

        val receipt = result.receipt ?: throw AssertionError("Receipt was not generated")
        assertEquals(4, receipt.token)
        assertEquals(450, receipt.consultationFee)
        assertEquals(PaymentMethod.UPI, receipt.paymentMethod)
        val admitted = model.uiState.appointments.single { it.id == "a4" }
        assertEquals(AppointmentStatus.ARRIVED, admitted.status)
        assertEquals(4, admitted.queueOrder)
        assertEquals("DL-20260715-M-004", admitted.receiptNumber)
        assertEquals(listOf(AuditAction.FEE_CONFIRMED, AuditAction.RECEIPT_GENERATED), model.uiState.auditEvents.map { it.action })

        val restored = DoctorViewModel(store, currentDate = { LocalDate.parse("2026-07-15") })
        assertEquals(PaymentStatus.PAID, restored.uiState.appointments.single { it.id == "a4" }.paymentStatus)
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
        assertEquals(7, receipt.token)
        assertEquals("DL-20260715-M-007", receipt.receiptNumber)
        val appointment = model.uiState.appointments.single { it.token == 7 }
        assertEquals(BookingSource.CLINIC_WALK_IN, appointment.bookingSource)
        assertEquals(AppointmentStatus.ARRIVED, appointment.status)
        assertEquals("9876512345", appointment.patientPhone)
        assertEquals(listOf(AuditAction.WALK_IN_BOOKED, AuditAction.FEE_CONFIRMED, AuditAction.RECEIPT_GENERATED), model.uiState.auditEvents.map { it.action })
        assertEquals(PaymentStatus.PAID, appointment.paymentStatus)
        assertEquals(500, appointment.consultationFee)

        val restored = DoctorViewModel(store, currentDate = { LocalDate.parse("2026-07-15") })
        assertEquals("DL-20260715-M-007", restored.uiState.appointments.single { it.token == 7 }.receiptNumber)
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
            token = 1,
            patientName = "Evening Patient",
            patientType = "Self",
            session = "Evening",
            status = AppointmentStatus.WAITING,
            bookedAt = "04:30 PM",
            queueOrder = 1,
            receiptNumber = "DL-20260715-E-001",
            consultationFee = 500,
            paymentStatus = PaymentStatus.PAID,
            paymentMethod = PaymentMethod.CASH,
            paidAt = "04:25 PM"
        )
        val initial = DummyData.initialState().copy(
            appointments = DummyData.appointments + eveningAppointment,
            sessionQueues = listOf(
                ConsultationQueue("Morning", QueueState.ACTIVE, 1),
                ConsultationQueue("Evening", QueueState.ACTIVE, 0)
            )
        )
        val model = DoctorViewModel(MemoryDoctorStateStore(initial))
        model.login(UserRole.DOCTOR)

        model.callNext("Evening")

        assertEquals(1, model.queueFor("Morning").currentToken)
        assertEquals(1, model.queueFor("Evening").currentToken)
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
                ConsultationQueue("Morning", QueueState.CLOSED, 6),
                ConsultationQueue("Evening", QueueState.CLOSED, 2)
            ),
            queueState = QueueState.CLOSED,
            currentToken = 6
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
        assertEquals(1, restored.queueFor("Morning").currentToken)
        assertEquals(0, restored.queueFor("Evening").currentToken)
    }
    @Test fun selectedSessionSurvivesNavigationStateAndRecreation() {
        val store = MemoryDoctorStateStore()
        val first = DoctorViewModel(store)
        first.login(UserRole.DOCTOR)
        first.selectSession("Evening")

        assertEquals("Evening", first.uiState.selectedSession)
        val restored = DoctorViewModel(store)
        assertEquals("Evening", restored.uiState.selectedSession)
        restored.selectSession("Morning")
        assertEquals("Morning", restored.uiState.selectedSession)
    }

    @Test fun morningAndEveningAllocateIndependentTokenSequences() {
        val model = DoctorViewModel(currentDate = { LocalDate.parse("2026-07-15") }, currentTime = { LocalTime.of(10, 0) })
        model.login(UserRole.DOCTOR)

        val morning = model.bookWalkIn(WalkInBookingRequest("Morning Walkin", "9876512345", "Self", "Morning", 500, PaymentMethod.CASH)).receipt
        val evening = model.bookWalkIn(WalkInBookingRequest("Evening Walkin", "9876512346", "Self", "Evening", 500, PaymentMethod.UPI)).receipt

        assertEquals(7, morning?.token)
        assertEquals(1, evening?.token)
        assertEquals("DL-20260715-M-007", morning?.receiptNumber)
        assertEquals("DL-20260715-E-001", evening?.receiptNumber)
    }
    @Test fun closingMorningLeavesEveningQueueAndBookingOpen() {
        val model = DoctorViewModel(
            currentDate = { LocalDate.parse("2026-07-15") },
            currentTime = { LocalTime.of(10, 0) }
        )
        model.login(UserRole.DOCTOR)

        assertTrue(model.closeSession("Morning"))

        assertEquals(QueueState.CLOSED, model.queueFor("Morning").state)
        assertEquals(QueueState.NOT_STARTED, model.queueFor("Evening").state)
        assertFalse(model.sessionBookingOpen("Morning"))
        assertTrue(model.sessionBookingOpen("Evening"))
        assertTrue(model.uiState.queueHistory.isEmpty())
        assertEquals(
            null,
            model.bookWalkIn(WalkInBookingRequest("Evening Patient", "9876512345", "Self", "Evening")).error
        )
    }

    @Test fun savedMaximumTokensBlocksWalkInAtExactSessionCapacity() {
        val model = DoctorViewModel(
            currentDate = { LocalDate.parse("2026-07-15") },
            currentTime = { LocalTime.of(10, 0) }
        )
        model.login(UserRole.DOCTOR)
        val clinic = model.uiState.clinics.first()

        assertEquals(null, model.updateClinic(clinic.copy(maxTokensPerSession = 6)))
        assertEquals(0, model.sessionRemainingCapacity("Morning"))
        assertFalse(model.sessionBookingOpen("Morning"))

        val blocked = model.bookWalkIn(WalkInBookingRequest("Capacity Patient", "9876512345", "Self", "Morning"))
        assertTrue(blocked.error?.contains("limit of 6 tokens") == true)
        assertEquals(6, model.sessionAppointmentCount("Morning"))
    }

    @Test fun notificationsTrackUnreadActivityAndPersistReadPosition() {
        val store = MemoryDoctorStateStore()
        val model = DoctorViewModel(store)
        model.login(UserRole.DOCTOR)

        model.callNext("Morning")
        assertTrue(model.unreadNotificationCount() > 0)
        model.markAllNotificationsRead()
        assertEquals(0, model.unreadNotificationCount())

        val restored = DoctorViewModel(store)
        assertEquals(0, restored.unreadNotificationCount())
        restored.toggleQueue("Morning")
        assertEquals(1, restored.unreadNotificationCount())
        restored.markNotificationRead(restored.uiState.auditEvents.last().sequence)
        assertEquals(0, restored.unreadNotificationCount())
    }

    @Test fun availabilityBlockDisablesOnlyMatchingSessionAndFlagsAffectedPatients() {
        val model = DoctorViewModel(
            currentDate = { LocalDate.parse("2026-07-15") },
            currentTime = { LocalTime.of(10, 0) }
        )
        model.login(UserRole.DOCTOR)

        val result = model.saveAvailabilityBlock(
            AvailabilityBlock("", "clinic-1", "2026-07-15", "2026-07-15", "Morning", "Doctor unavailable", false)
        )

        assertEquals(null, result)
        val block = model.uiState.availabilityBlocks.single { it.reason == "Doctor unavailable" }
        assertFalse(model.sessionBookingOpen("Morning"))
        assertTrue(model.sessionBookingOpen("Evening"))
        assertEquals(5, model.uiState.appointments.count { it.availabilityBlockId == block.id })
        assertTrue(
            model.uiState.appointments
                .filter { it.availabilityBlockId == block.id }
                .all { it.availabilityImpactStatus == AvailabilityImpactStatus.CONTACT_PENDING }
        )
        val blocked = model.bookWalkIn(WalkInBookingRequest("Blocked Patient", "9876512345", "Self", "Morning"))
        assertTrue(blocked.error?.contains("Doctor unavailable") == true)
        assertEquals(AuditAction.AVAILABILITY_SAVED, model.uiState.auditEvents.last().action)
    }

    @Test fun reopeningAvailabilityClearsAffectedFlagsAndRestoresBooking() {
        val model = DoctorViewModel(
            currentDate = { LocalDate.parse("2026-07-15") },
            currentTime = { LocalTime.of(10, 0) }
        )
        model.login(UserRole.DOCTOR)
        model.saveAvailabilityBlock(
            AvailabilityBlock("", "clinic-1", "2026-07-15", "2026-07-15", "Morning", "Emergency leave", false)
        )
        val block = model.uiState.availabilityBlocks.single { it.reason == "Emergency leave" }

        assertTrue(model.setAvailabilityAppointmentsEnabled(block.id, true))

        assertTrue(model.sessionBookingOpen("Morning"))
        assertTrue(model.uiState.appointments.all { it.availabilityBlockId != block.id })
        assertTrue(model.uiState.appointments.all { it.availabilityImpactStatus == AvailabilityImpactStatus.NONE })
        assertEquals(AuditAction.AVAILABILITY_CHANGED, model.uiState.auditEvents.last().action)
    }

    @Test fun affectedPatientFollowUpAndAvailabilityBlocksPersist() {
        val store = MemoryDoctorStateStore()
        val first = DoctorViewModel(
            store,
            currentDate = { LocalDate.parse("2026-07-15") },
            currentTime = { LocalTime.of(10, 0) }
        )
        first.login(UserRole.DOCTOR)
        first.saveAvailabilityBlock(
            AvailabilityBlock("", "clinic-1", "2026-07-15", "2026-07-16", "Both", "Regional training workshop", false)
        )
        val block = first.uiState.availabilityBlocks.single { it.reason == "Regional training workshop" }
        assertTrue(first.updateAffectedPatientStatus("a2", AvailabilityImpactStatus.PATIENT_NOTIFIED))

        val restored = DoctorViewModel(
            store,
            currentDate = { LocalDate.parse("2026-07-15") },
            currentTime = { LocalTime.of(10, 5) }
        )

        assertEquals(block, restored.uiState.availabilityBlocks.single { it.id == block.id })
        assertEquals(AvailabilityImpactStatus.PATIENT_NOTIFIED, restored.uiState.appointments.single { it.id == "a2" }.availabilityImpactStatus)
        assertEquals(AuditAction.AFFECTED_PATIENT_UPDATED, restored.uiState.auditEvents.last().action)
    }

    @Test fun invalidOrAssistantAvailabilityChangesAreRejected() {
        val model = DoctorViewModel(currentDate = { LocalDate.parse("2026-07-15") })
        model.login(UserRole.DOCTOR)
        val originalCount = model.uiState.availabilityBlocks.size

        val invalid = model.saveAvailabilityBlock(
            AvailabilityBlock("", "clinic-1", "2026-07-20", "2026-07-19", "Morning", "Away", false)
        )
        assertTrue(invalid != null)
        assertEquals(originalCount, model.uiState.availabilityBlocks.size)

        model.login(UserRole.ASSISTANT, "staff-1")
        val unauthorized = model.saveAvailabilityBlock(
            AvailabilityBlock("", "clinic-1", "2026-07-20", "2026-07-20", "Morning", "Assistant block", false)
        )
        assertTrue(unauthorized != null)
        assertEquals(originalCount, model.uiState.availabilityBlocks.size)
    }

    @Test fun affectedPatientsAreNotCalledUntilAvailabilityFollowUpIsResolved() {
        val model = DoctorViewModel(
            currentDate = { LocalDate.parse("2026-07-15") },
            currentTime = { LocalTime.of(10, 0) }
        )
        model.login(UserRole.DOCTOR)
        model.saveAvailabilityBlock(
            AvailabilityBlock(
                id = "",
                clinicId = "clinic-1",
                fromDate = "2026-07-15",
                toDate = "2026-07-15",
                sessions = "Morning",
                reason = "Unexpected emergency",
                appointmentsEnabled = false
            )
        )

        model.updateAppointment("a2", AppointmentStatus.SKIPPED)
        assertEquals(AppointmentStatus.WAITING, model.uiState.appointments.single { it.id == "a2" }.status)

        model.callNext("Morning")

        assertEquals(1, model.uiState.currentToken)
        assertEquals(AppointmentStatus.COMPLETED, model.uiState.appointments.single { it.id == "a1" }.status)
        assertEquals(AppointmentStatus.WAITING, model.uiState.appointments.single { it.id == "a2" }.status)

        assertTrue(model.updateAffectedPatientStatus("a2", AvailabilityImpactStatus.RESOLVED))
        model.callNext("Morning")

        assertEquals(2, model.uiState.currentToken)
        assertEquals(AppointmentStatus.IN_CONSULTATION, model.uiState.appointments.single { it.id == "a2" }.status)
    }


    @Test fun announcementLifecycleControlsPatientProfileFeedAndAudit() {
        val model = DoctorViewModel(
            currentDate = { LocalDate.parse("2026-07-15") },
            currentTime = { LocalTime.of(10, 30) }
        )
        model.login(UserRole.DOCTOR)
        val result = model.saveAnnouncement(
            Announcement(
                id = "",
                title = "Priority wellness clinic",
                message = "Extended preventive consultations are available this week.",
                type = AnnouncementType.OFFER,
                startsOn = "2026-07-15",
                endsOn = "2026-07-16",
                active = true
            )
        )

        assertEquals(null, result)
        val saved = model.uiState.announcements.single { it.title == "Priority wellness clinic" }
        assertEquals(AnnouncementPublicationStatus.LIVE, model.announcementPublicationStatus(saved))
        val feedItem = model.patientProfileFeed().single { it.announcementId == saved.id }
        assertEquals(model.uiState.profile.name, feedItem.doctorName)
        assertEquals("clinic-1", feedItem.clinicId)
        assertEquals(AuditAction.ANNOUNCEMENT_SAVED, model.uiState.auditEvents.last().action)

        assertTrue(model.setAnnouncementActive(saved.id, false))
        assertTrue(model.patientProfileFeed().none { it.announcementId == saved.id })
        assertEquals(AuditAction.ANNOUNCEMENT_VISIBILITY_CHANGED, model.uiState.auditEvents.last().action)

        assertTrue(model.deleteAnnouncement(saved.id))
        assertTrue(model.uiState.announcements.none { it.id == saved.id })
        assertEquals(AuditAction.ANNOUNCEMENT_DELETED, model.uiState.auditEvents.last().action)
    }

    @Test fun scheduledAndExpiredAnnouncementsAreExcludedFromPatientFeed() {
        val model = DoctorViewModel(currentDate = { LocalDate.parse("2026-07-15") })
        model.login(UserRole.DOCTOR)
        model.saveAnnouncement(
            Announcement("", "Future health camp", "Screening appointments open from tomorrow.", AnnouncementType.CAMP, "2026-07-16", "2026-07-17", true)
        )
        model.saveAnnouncement(
            Announcement("", "Previous clinic offer", "This completed offer remains in update history.", AnnouncementType.OFFER, "2026-07-10", "2026-07-14", true)
        )

        val scheduled = model.uiState.announcements.single { it.title == "Future health camp" }
        val expired = model.uiState.announcements.single { it.title == "Previous clinic offer" }

        assertEquals(AnnouncementPublicationStatus.SCHEDULED, model.announcementPublicationStatus(scheduled))
        assertEquals(AnnouncementPublicationStatus.EXPIRED, model.announcementPublicationStatus(expired))
        assertTrue(model.patientProfileFeed().none { it.announcementId in setOf(scheduled.id, expired.id) })
    }

    @Test fun announcementValidationAndPermissionsAreEnforced() {
        val model = DoctorViewModel(currentDate = { LocalDate.parse("2026-07-15") })
        model.login(UserRole.DOCTOR)
        val originalCount = model.uiState.announcements.size

        val invalid = model.saveAnnouncement(
            Announcement("", "Bad", "Short", AnnouncementType.GENERAL, "2026-07-16", "2026-07-15", true)
        )
        assertTrue(invalid != null)
        assertEquals(originalCount, model.uiState.announcements.size)

        model.login(UserRole.ASSISTANT, "staff-2")
        val unauthorized = model.saveAnnouncement(
            Announcement("", "Clinic schedule update", "The clinic schedule has been updated for patients.", AnnouncementType.GENERAL, "2026-07-15", "2026-07-16", true)
        )
        assertTrue(unauthorized != null)
        assertEquals(originalCount, model.uiState.announcements.size)
    }

    @Test fun completeAnnouncementRecordsPersistAcrossViewModelRecreation() {
        val store = MemoryDoctorStateStore()
        val first = DoctorViewModel(
            store,
            currentDate = { LocalDate.parse("2026-07-15") },
            currentTime = { LocalTime.of(11, 0) }
        )
        first.login(UserRole.DOCTOR)
        first.saveAnnouncement(
            Announcement("", "Weekend availability", "The doctor is available for a special weekend session.", AnnouncementType.AVAILABILITY, "2026-07-18", "2026-07-19", false)
        )
        val saved = first.uiState.announcements.single { it.title == "Weekend availability" }

        val restored = DoctorViewModel(store, currentDate = { LocalDate.parse("2026-07-15") })

        assertEquals(saved, restored.uiState.announcements.single { it.id == saved.id })
        assertEquals(AnnouncementPublicationStatus.DRAFT, restored.announcementPublicationStatus(saved))
    }

    @Test fun doctorCreatesPersistedAssistantWithGeneratedCredentials() {
        val store = MemoryDoctorStateStore()
        val first = DoctorViewModel(
            store,
            currentDate = { LocalDate.parse("2026-07-16") },
            currentTime = { LocalTime.of(12, 0) },
            pinGenerator = { "4826" }
        )
        first.login(UserRole.DOCTOR)

        val result = first.createAssistant(
            "  Anita   Singh  ",
            "98765 09999",
            setOf(Permission.VIEW_QUEUE, Permission.VIEW_TODAY_APPOINTMENTS)
        )
        val credential = result.credential ?: throw AssertionError(result.error)
        assertEquals("Anita Singh", credential.assistant.name)
        assertEquals("9876509999", credential.assistant.phone)
        assertEquals("4826", credential.temporaryPin)
        assertTrue(first.uiState.auditEvents.any { it.action == AuditAction.ASSISTANT_CREATED })

        val restored = DoctorViewModel(store)
        assertEquals(credential.assistant, restored.uiState.assistants.single { it.id == credential.assistant.id })
    }

    @Test fun assistantCreationValidationAndDoctorAuthorizationAreEnforced() {
        val model = DoctorViewModel(pinGenerator = { "4826" })
        model.login(UserRole.ASSISTANT, "staff-1")
        assertTrue(model.createAssistant("Anita Singh", "9876509999", emptySet()).error != null)

        model.login(UserRole.DOCTOR)
        assertTrue(model.createAssistant("A", "123", emptySet()).error != null)
        val duplicate = model.createAssistant("Another Assistant", model.uiState.assistants.first().phone, emptySet())
        assertTrue(duplicate.error != null)
    }

    @Test fun doctorCanDisableResetAndAuditAssistantAccess() {
        val model = DoctorViewModel(pinGenerator = { "7391" })
        model.login(UserRole.DOCTOR)
        val assistant = model.uiState.assistants.first()

        assertTrue(model.setAssistantActive(assistant.id, false))
        assertFalse(model.uiState.assistants.first { it.id == assistant.id }.active)
        val credential = model.resetAssistantPin(assistant.id) ?: throw AssertionError("PIN was not generated")
        assertEquals("7391", credential.temporaryPin)
        assertTrue(model.uiState.auditEvents.any { it.action == AuditAction.ASSISTANT_STATUS_CHANGED })
        assertTrue(model.uiState.auditEvents.any { it.action == AuditAction.ASSISTANT_PIN_RESET })

        model.login(UserRole.ASSISTANT, assistant.id)
        assertFalse(model.setAssistantActive(assistant.id, true))
        assertEquals(null, model.resetAssistantPin(assistant.id))
    }

    @Test fun assistantClinicAccessFollowsGrantedPermissionButEditingRemainsDoctorOnly() {
        val model = DoctorViewModel()
        model.login(UserRole.DOCTOR)
        val assistant = model.uiState.assistants.first()
        model.togglePermission(assistant.id, Permission.MANAGE_CLINIC_AVAILABILITY)
        model.login(UserRole.ASSISTANT, assistant.id)

        assertTrue(model.canAccessClinic())
        val original = model.uiState.clinics.first()
        assertTrue(model.updateClinic(original.copy(name = "Unauthorized edit")) != null)
        assertEquals(original, model.uiState.clinics.first())
    }

    @Test fun reportAccessAndOperationalMetricsRespectPermissions() {
        val model = DoctorViewModel()
        model.login(UserRole.DOCTOR)
        val report = model.operationalReport()

        assertEquals(6, report.appointments)
        assertEquals(3, report.pending)
        assertEquals(1500, report.collectedFees)
        assertEquals(3, report.feedbackCount)

        val assistant = model.uiState.assistants.first()
        model.login(UserRole.ASSISTANT, assistant.id)
        assertFalse(model.canAccessReports())
        model.login(UserRole.DOCTOR)
        model.togglePermission(assistant.id, Permission.VIEW_REPORTS)
        model.login(UserRole.ASSISTANT, assistant.id)
        assertTrue(model.canAccessReports())
    }

    @Test fun feedbackAcknowledgementIsAuthorizedAuditedAndPersisted() {
        val store = MemoryDoctorStateStore()
        val model = DoctorViewModel(store)
        model.login(UserRole.DOCTOR)
        val feedback = model.uiState.feedback.first()

        assertTrue(model.acknowledgeFeedback(feedback.id))
        assertTrue(model.uiState.feedback.first { it.id == feedback.id }.acknowledged)
        assertEquals(AuditAction.FEEDBACK_ACKNOWLEDGED, model.uiState.auditEvents.last().action)

        val restored = DoctorViewModel(store)
        assertTrue(restored.uiState.feedback.first { it.id == feedback.id }.acknowledged)

        restored.login(UserRole.ASSISTANT, "staff-2")
        assertFalse(restored.acknowledgeFeedback(restored.uiState.feedback.last().id))
    }

    @Test fun queueDelayNoticeValidationPermissionAndPersistenceAreEnforced() {
        val store = MemoryDoctorStateStore()
        val model = DoctorViewModel(
            store,
            currentDate = { LocalDate.parse("2026-07-16") },
            currentTime = { LocalTime.of(18, 20) }
        )
        model.login(UserRole.ASSISTANT, "staff-2")
        assertTrue(model.sendQueueDelayNotice("Evening", 25, "Queue running late.") != null)

        model.login(UserRole.DOCTOR)
        assertTrue(model.sendQueueDelayNotice("Evening", 2, "Late") != null)
        assertEquals(null, model.sendQueueDelayNotice("Evening", 25, "Queue running later than expected."))
        val notice = model.uiState.queueDelayNotices.single()
        assertEquals(25, notice.delayMinutes)
        assertEquals("Evening", notice.session)
        assertEquals(AuditAction.QUEUE_DELAY_NOTICE_SENT, model.uiState.auditEvents.last().action)

        val restored = DoctorViewModel(store)
        assertEquals(notice, restored.uiState.queueDelayNotices.single())
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
