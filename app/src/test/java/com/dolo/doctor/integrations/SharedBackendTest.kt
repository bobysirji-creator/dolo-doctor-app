package com.dolo.doctor.integrations

import com.dolo.doctor.data.DummyData
import com.dolo.doctor.data.model.AppointmentStatus
import com.dolo.doctor.data.model.BookingSource
import com.dolo.doctor.data.model.PaymentStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SharedBackendTest {
    private fun snapshot(revision: Long = 0) = SharedClinicSnapshot(
        revision = revision,
        generatedAt = "2026-07-16T10:00:00Z",
        clinic = DummyData.clinics.first(),
        queueDate = "2026-07-16",
        queues = DummyData.initialState("2026-07-16").sessionQueues,
        appointments = DummyData.appointments,
        announcements = DummyData.announcements,
        availabilityBlocks = DummyData.availabilityBlocks,
        delayNotices = emptyList()
    )

    @Test fun publishUsesRevisionAndIdempotencyRules() {
        val gateway = LocalMockSharedBackendGateway { "2026-07-16T10:01:00Z" }
        val command = PublishClinicCommand("publish-1", 0, snapshot())

        val first = gateway.publish(command) as SharedBackendResult.Success<SharedClinicSnapshot>
        val replay = gateway.publish(command) as SharedBackendResult.Success<SharedClinicSnapshot>
        val conflict = gateway.publish(PublishClinicCommand("publish-2", 0, snapshot()))

        assertEquals(1L, first.value.revision)
        assertFalse(first.replayed)
        assertTrue(replay.replayed)
        assertEquals(first.value, replay.value)
        assertTrue(conflict is SharedBackendResult.Conflict)
        assertEquals(1L, (conflict as SharedBackendResult.Conflict).serverRevision)
    }

    @Test fun patientBookingGetsIndependentSessionTokenAndCannotDuplicate() {
        val gateway = LocalMockSharedBackendGateway { "2026-07-16T10:01:00Z" }
        val published = gateway.publish(PublishClinicCommand("publish-1", 0, snapshot()))
            as SharedBackendResult.Success<SharedClinicSnapshot>
        val command = PatientBookingCommand(
            idempotencyKey = "booking-1",
            baseRevision = published.value.revision,
            clinicId = "clinic-1",
            appointmentDate = "2026-07-16",
            session = "Evening",
            patientName = "Online Patient",
            patientPhone = "9876502222",
            patientType = "Self",
            bookedAt = "06:00 PM"
        )

        val booked = gateway.bookFromPatientApp(command) as SharedBackendResult.Success<SharedClinicSnapshot>
        val replay = gateway.bookFromPatientApp(command) as SharedBackendResult.Success<SharedClinicSnapshot>
        val appointment = booked.value.appointments.single { it.id == "patient-booking-1" }

        assertEquals(2L, booked.value.revision)
        assertEquals(1, appointment.token)
        assertEquals(0, appointment.queueOrder)
        assertEquals(BookingSource.PATIENT_APP, appointment.bookingSource)
        assertEquals(AppointmentStatus.BOOKED, appointment.status)
        assertEquals(PaymentStatus.PENDING, appointment.paymentStatus)
        assertTrue(appointment.receiptNumber.isBlank())
        assertTrue(replay.replayed)
        assertEquals(1, replay.value.appointments.count { it.id == appointment.id })
    }

    @Test fun closedSessionRejectsPatientBooking() {
        val gateway = LocalMockSharedBackendGateway()
        val closed = snapshot().copy(
            queues = snapshot().queues.map {
                if (it.session == "Evening") it.copy(state = com.dolo.doctor.data.model.QueueState.CLOSED) else it
            }
        )
        val published = gateway.publish(PublishClinicCommand("publish-1", 0, closed))
            as SharedBackendResult.Success<SharedClinicSnapshot>

        val result = gateway.bookFromPatientApp(
            PatientBookingCommand(
                "booking-closed",
                published.value.revision,
                "clinic-1",
                "2026-07-16",
                "Evening",
                "Online Patient",
                "9876502222",
                "Self",
                "06:00 PM"
            )
        )

        assertTrue(result is SharedBackendResult.Failure)
    }

    @Test fun mockEnforcesFutureBookingPolicyWithoutMixingClinicDays() {
        val disabledGateway = LocalMockSharedBackendGateway()
        val disabledPublished = disabledGateway.publish(
            PublishClinicCommand("publish-disabled", 0, snapshot())
        ) as SharedBackendResult.Success<SharedClinicSnapshot>
        val disabledResult = disabledGateway.bookFromPatientApp(
            PatientBookingCommand(
                "future-disabled",
                disabledPublished.value.revision,
                "clinic-1",
                "2026-07-17",
                "Morning",
                "Future Patient",
                "9876503333",
                "Self",
                "09:00 AM"
            )
        )
        assertTrue(disabledResult is SharedBackendResult.Failure)
        assertTrue((disabledResult as SharedBackendResult.Failure).message.contains("current day"))

        val enabledGateway = LocalMockSharedBackendGateway()
        val enabledSnapshot = snapshot().copy(
            clinic = snapshot().clinic.copy(
                futureBookingEnabled = true,
                advanceBookingDays = 10
            )
        )
        val enabledPublished = enabledGateway.publish(
            PublishClinicCommand("publish-enabled", 0, enabledSnapshot)
        ) as SharedBackendResult.Success<SharedClinicSnapshot>
        val enabledResult = enabledGateway.bookFromPatientApp(
            PatientBookingCommand(
                "future-enabled",
                enabledPublished.value.revision,
                "clinic-1",
                "2026-07-20",
                "Morning",
                "Future Patient",
                "9876503333",
                "Self",
                "09:00 AM"
            )
        )
        assertTrue(enabledResult is SharedBackendResult.Failure)
        assertTrue((enabledResult as SharedBackendResult.Failure).message.contains("hosted backend"))
        val unchanged = enabledGateway.pull("clinic-1")
            as SharedBackendResult.Success<SharedClinicSnapshot>
        assertEquals(enabledPublished.value.appointments, unchanged.value.appointments)
    }
}
