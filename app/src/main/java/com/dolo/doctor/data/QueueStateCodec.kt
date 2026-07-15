package com.dolo.doctor.data

import com.dolo.doctor.data.model.Appointment
import com.dolo.doctor.data.model.AppointmentStatus
import com.dolo.doctor.data.model.DailyQueueHistory
import java.nio.charset.StandardCharsets
import java.util.Base64

internal object QueueStateCodec {
    private val encoder = Base64.getUrlEncoder().withoutPadding()
    private val decoder = Base64.getUrlDecoder()

    fun encodeAppointment(appointment: Appointment): String = listOf(
        appointment.id,
        appointment.token.toString(),
        appointment.patientName,
        appointment.patientType,
        appointment.session,
        appointment.status.name,
        appointment.bookedAt
    ).joinToString(",") { encode(it) }

    fun decodeAppointment(value: String): Appointment? {
        val fields = value.split(",").mapNotNull(::decode)
        if (fields.size != 7) return null
        val token = fields[1].toIntOrNull() ?: return null
        val status = runCatching { AppointmentStatus.valueOf(fields[5]) }.getOrNull() ?: return null
        return Appointment(fields[0], token, fields[2], fields[3], fields[4], status, fields[6])
    }

    fun encodeHistory(history: DailyQueueHistory): String = listOf(
        history.date,
        history.clinicName,
        history.closedAt,
        history.closureReason,
        history.finalToken.toString(),
        history.appointments.joinToString(";") { encodeAppointment(it) }
    ).joinToString("|") { encode(it) }

    fun decodeHistory(value: String): DailyQueueHistory? {
        val fields = value.split("|").mapNotNull(::decode)
        if (fields.size != 6) return null
        val finalToken = fields[4].toIntOrNull() ?: return null
        val appointments = if (fields[5].isBlank()) emptyList() else {
            fields[5].split(";").mapNotNull(::decodeAppointment)
        }
        return DailyQueueHistory(fields[0], fields[1], fields[2], fields[3], finalToken, appointments)
    }

    private fun encode(value: String): String =
        encoder.encodeToString(value.toByteArray(StandardCharsets.UTF_8))

    private fun decode(value: String): String? = runCatching {
        String(decoder.decode(value), StandardCharsets.UTF_8)
    }.getOrNull()
}