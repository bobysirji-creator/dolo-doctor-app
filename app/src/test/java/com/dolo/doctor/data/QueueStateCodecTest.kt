package com.dolo.doctor.data

import com.dolo.doctor.data.model.Appointment
import com.dolo.doctor.data.model.AppointmentStatus
import com.dolo.doctor.data.model.DailyQueueHistory
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
            bookedAt = "06:15 PM"
        )

        assertEquals(appointment, QueueStateCodec.decodeAppointment(QueueStateCodec.encodeAppointment(appointment)))
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

    @Test fun malformedValuesAreIgnored() {
        assertEquals(null, QueueStateCodec.decodeAppointment("invalid"))
        assertEquals(null, QueueStateCodec.decodeHistory("invalid"))
    }
}