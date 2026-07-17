package com.dolo.doctor.data

import com.dolo.doctor.data.model.Appointment
import com.dolo.doctor.data.model.AppointmentStatus
import com.dolo.doctor.data.model.DailyQueueHistory
import com.dolo.doctor.data.model.AuditAction
import com.dolo.doctor.data.model.QueueAuditEvent
import com.dolo.doctor.data.model.PaymentMethod
import com.dolo.doctor.data.model.AvailabilityBlock
import com.dolo.doctor.data.model.AvailabilityImpactStatus
import com.dolo.doctor.data.model.Announcement
import com.dolo.doctor.data.model.AnnouncementType
import com.dolo.doctor.data.model.PaymentStatus
import com.dolo.doctor.data.model.Assistant
import com.dolo.doctor.data.model.Permission
import com.dolo.doctor.data.model.PatientFeedback
import com.dolo.doctor.data.model.QueueDelayNotice
import com.dolo.doctor.data.model.WeeklyClosureScope
import java.time.DayOfWeek
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
            receiptNumber = "DL-20260715-E-042",
            consultationFee = 500,
            paymentStatus = PaymentStatus.PAID,
            paymentMethod = PaymentMethod.UPI,
            paidAt = "06:10 PM",
            availabilityBlockId = "block-1",
            availabilityImpactStatus = AvailabilityImpactStatus.PATIENT_NOTIFIED,
            availabilityUpdatedAt = "07:00 PM",
            lateQueuePlacement = true,
            lateArrivalAnchorToken = 8
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
    @Test fun legacyReceiptInfersPaidStatusForMigration() {
        val fields = listOf("legacy-paid", "12", "Old Patient", "Self", "Morning", "WAITING", "08:00 AM", "12", "PATIENT_APP", "9876500000", "DL-OLD-012")
        val legacy = fields.joinToString(",") { java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(it.toByteArray()) }

        val decoded = QueueStateCodec.decodeAppointment(legacy) ?: throw AssertionError("Legacy paid appointment was not decoded")

        assertEquals(PaymentStatus.PAID, decoded.paymentStatus)
        assertEquals(PaymentMethod.CASH, decoded.paymentMethod)
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
            averageConsultationMinutes = 15,
            futureBookingEnabled = true,
            advanceBookingDays = 21,
            weeklyClosures = mapOf(
                DayOfWeek.SUNDAY to WeeklyClosureScope.BOTH,
                DayOfWeek.WEDNESDAY to WeeklyClosureScope.EVENING
            )
        )

        assertEquals(profile, QueueStateCodec.decodeProfile(QueueStateCodec.encodeProfile(profile)))
        assertEquals(clinic, QueueStateCodec.decodeClinic(QueueStateCodec.encodeClinic(clinic)))
    }

    @Test fun legacyClinicDefaultsToCurrentDayOnly() {
        val fields = listOf(
            "clinic-old", "Old Clinic", "22 Green Park, New Delhi", "01140002200",
            "09:00 AM - 01:00 PM", "05:00 PM - 09:00 PM", "30", "12"
        )
        val legacy = fields.joinToString("|") {
            java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(it.toByteArray())
        }

        val decoded = QueueStateCodec.decodeClinic(legacy)
            ?: throw AssertionError("Legacy clinic was not decoded")

        assertEquals(false, decoded.futureBookingEnabled)
        assertEquals(7, decoded.advanceBookingDays)
        assertEquals(emptyMap<DayOfWeek, WeeklyClosureScope>(), decoded.weeklyClosures)
    }
    @Test fun stageElevenClinicDefaultsToOpenWeeklySchedule() {
        val fields = listOf(
            "clinic-stage11", "Stage 11 Clinic", "22 Green Park, New Delhi", "01140002200",
            "09:00 AM - 01:00 PM", "05:00 PM - 09:00 PM", "30", "12", "true", "14"
        )
        val legacy = fields.joinToString("|") {
            java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(it.toByteArray())
        }

        val decoded = QueueStateCodec.decodeClinic(legacy)
            ?: throw AssertionError("Stage 11 clinic was not decoded")

        assertEquals(true, decoded.futureBookingEnabled)
        assertEquals(14, decoded.advanceBookingDays)
        assertEquals(emptyMap<DayOfWeek, WeeklyClosureScope>(), decoded.weeklyClosures)
    }
    @Test fun announcementRoundTripKeepsCompletePublishedRecord() {
        val announcement = Announcement(
            "announcement-1", "Free heart health camp", "Screening is available at the clinic.",
            AnnouncementType.CAMP, "2026-07-20", "2026-07-21", true
        )

        assertEquals(announcement, QueueStateCodec.decodeAnnouncement(QueueStateCodec.encodeAnnouncement(announcement)))
    }


    @Test fun availabilityBlockRoundTripKeepsBookingState() {
        val block = AvailabilityBlock("block-1", "clinic-1", "2026-07-20", "2026-07-22", "Both", "Medical conference", false)

        assertEquals(
            block,
            QueueStateCodec.decodeAvailabilityBlock(QueueStateCodec.encodeAvailabilityBlock(block))
        )
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
    @Test fun assistantRoundTripKeepsIdentityStatusAndPermissions() {
        val assistant = Assistant(
            "staff-new", "Anita Singh", "9876509999", false,
            setOf(Permission.VIEW_QUEUE, Permission.VIEW_TODAY_APPOINTMENTS)
        )

        assertEquals(assistant, QueueStateCodec.decodeAssistant(QueueStateCodec.encodeAssistant(assistant)))
    }

    @Test fun feedbackAndDelayNoticeRoundTripsKeepStageNineFields() {
        val feedback = PatientFeedback("f1", "clinic-1", "Patient", 5, "Very helpful consultation.", "2026-07-16", true)
        val notice = QueueDelayNotice("d1", "clinic-1", "Evening", 25, "Queue running late.", "2026-07-16", "06:20 PM", "Dr. Aisha Mehta")

        assertEquals(feedback, QueueStateCodec.decodeFeedback(QueueStateCodec.encodeFeedback(feedback)))
        assertEquals(notice, QueueStateCodec.decodeQueueDelayNotice(QueueStateCodec.encodeQueueDelayNotice(notice)))
    }
    @Test fun malformedValuesAreIgnored() {
        assertEquals(null, QueueStateCodec.decodeAppointment("invalid"))
        assertEquals(null, QueueStateCodec.decodeHistory("invalid"))
    }
}
