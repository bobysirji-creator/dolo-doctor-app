package com.dolo.doctor.data

import com.dolo.doctor.data.model.Appointment
import com.dolo.doctor.data.model.AppointmentStatus
import com.dolo.doctor.data.model.DailyQueueHistory
import com.dolo.doctor.data.model.AuditAction
import com.dolo.doctor.data.model.QueueAuditEvent
import org.junit.Assert.assertEquals
import org.junit.Test

class QueueStateCodecTest {
    @Test fun appointmentRoundTripKeepsEveryField() {
        val appointment = Appointment(
            id = "appointment-1",
            token = 42,
            patientName = "Aarav & Meera",
            patientType = "Family member",
            session = "Evening",
            status = AppointmentStatus.IN_CONSULTATION,
            bookedAt = "06:15 PM",
            queueOrder = 57,
            bookingSource = com.dolo.doctor.data.model.BookingSource.CLINIC_WALK_IN,
            patientPhone = "9876512345",
            receiptNumber = "DL-20260715-042"
        )

        assertEquals(appointment, QueueStateCodec.decodeAppointment(QueueStateCodec.encodeAppointment(appointment)))
    }

    @Test fun legacySevenFieldAppointmentMigratesWithTokenQueueOrder() {
        val legacy = listOf("legacy", "8", "Old Patient", "Self", "Morning", "WAITING", "08:00 AM")
            .joinToString(",") { java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(it.toByteArray()) }

        val decoded = QueueStateCodec.decodeAppointment(legacy) ?: throw AssertionError("Legacy appointment was not decoded")

        assertEquals(8, decoded.queueOrder)
        assertEquals(com.dolo.doctor.data.model.BookingSource.PATIENT_APP, decoded.bookingSource)
        assertEquals("", decoded.receiptNumber)
    }
    @Test fun dailyHistoryRoundTripKeepsAppointments() {
        val history = DailyQueueHistory(
            date = "2026-07-15",
            clinicName = "Care Point Clinic",
            closedAt = "09:30 PM",
            closureReason = "Closed manually by doctor",
            finalToken = 14,
            appointments = DummyData.appointments
        )

        assertEquals(history, QueueStateCodec.decodeHistory(QueueStateCodec.encodeHistory(history)))
    }

    @Test fun profileAndClinicRoundTripsKeepEditableFields() {
        val profile = DummyData.profile.copy(
            name = "Dr. Updated Name",
            specialty = "Internal Medicine",
            consultationFee = 700,
            reviewStatus = com.dolo.doctor.data.model.ProfileReviewStatus.PENDING_REVIEW
        )
        val clinic = DummyData.clinics.first().copy(
            morningSession = "08:30 AM - 12:30 PM",
            maxTokensPerSession = 40,
            averageConsultationMinutes = 15
        )

        assertEquals(profile, QueueStateCodec.decodeProfile(QueueStateCodec.encodeProfile(profile)))
        assertEquals(clinic, QueueStateCodec.decodeClinic(QueueStateCodec.encodeClinic(clinic)))
    }

    @Test fun sessionQueueRoundTripKeepsIndependentState() {
        val queue = com.dolo.doctor.data.model.ConsultationQueue("Evening", com.dolo.doctor.data.model.QueueState.PAUSED, 23)

        assertEquals(queue, QueueStateCodec.decodeSessionQueue(QueueStateCodec.encodeSessionQueue(queue)))
    }
    @Test fun auditEventRoundTripKeepsActorAndTransitionContext() {
        val event = QueueAuditEvent(
            id = "2026-07-15-42",
            sequence = 42,
            date = "2026-07-15",
            time = "09:05 AM",
            actor = "Neha Kapoor",
            action = AuditAction.STATUS_CHANGED,
            token = 12,
            patientName = "Aman Gupta",
            fromStatus = AppointmentStatus.BOOKED,
            toStatus = AppointmentStatus.ARRIVED,
            detail = "Changed queue status"
        )

        assertEquals(event, QueueStateCodec.decodeAuditEvent(QueueStateCodec.encodeAuditEvent(event)))
    }
    @Test fun malformedValuesAreIgnored() {
        assertEquals(null, QueueStateCodec.decodeAppointment("invalid"))
        assertEquals(null, QueueStateCodec.decodeHistory("invalid"))
    }
}
