package com.dolo.doctor.data

import com.dolo.doctor.data.model.Appointment
import com.dolo.doctor.data.model.AppointmentStatus
import com.dolo.doctor.data.model.DailyQueueHistory
import com.dolo.doctor.data.model.DoctorProfile
import com.dolo.doctor.data.model.Clinic
import com.dolo.doctor.data.model.ProfileReviewStatus
import com.dolo.doctor.data.model.QueueAuditEvent
import com.dolo.doctor.data.model.AuditAction
import java.nio.charset.StandardCharsets
import java.util.Base64

internal object QueueStateCodec {
    private val encoder = Base64.getUrlEncoder().withoutPadding()
    private val decoder = Base64.getUrlDecoder()


    fun encodeProfile(profile: DoctorProfile): String = listOf(
        profile.name,
        profile.specialty,
        profile.qualification,
        profile.registrationNumber,
        profile.experienceYears.toString(),
        profile.consultationFee.toString(),
        profile.about,
        profile.verified.toString(),
        profile.reviewStatus.name
    ).joinToString("|") { encode(it) }

    fun decodeProfile(value: String): DoctorProfile? {
        val fields = value.split("|").mapNotNull(::decode)
        if (fields.size != 9) return null
        val experience = fields[4].toIntOrNull() ?: return null
        val fee = fields[5].toIntOrNull() ?: return null
        val verified = fields[7].toBooleanStrictOrNull() ?: return null
        val reviewStatus = runCatching { ProfileReviewStatus.valueOf(fields[8]) }.getOrNull() ?: return null
        return DoctorProfile(fields[0], fields[1], fields[2], fields[3], experience, fee, fields[6], verified, reviewStatus)
    }

    fun encodeClinic(clinic: Clinic): String = listOf(
        clinic.id,
        clinic.name,
        clinic.address,
        clinic.phone,
        clinic.morningSession,
        clinic.eveningSession,
        clinic.maxTokensPerSession.toString(),
        clinic.averageConsultationMinutes.toString()
    ).joinToString("|") { encode(it) }

    fun decodeClinic(value: String): Clinic? {
        val fields = value.split("|").mapNotNull(::decode)
        if (fields.size != 8) return null
        val maxTokens = fields[6].toIntOrNull() ?: return null
        val averageMinutes = fields[7].toIntOrNull() ?: return null
        return Clinic(fields[0], fields[1], fields[2], fields[3], fields[4], fields[5], maxTokens, averageMinutes)
    }

    fun encodeAuditEvent(event: QueueAuditEvent): String = listOf(
        event.id,
        event.sequence.toString(),
        event.date,
        event.time,
        event.actor,
        event.action.name,
        event.token?.toString().orEmpty(),
        event.patientName.orEmpty(),
        event.fromStatus?.name.orEmpty(),
        event.toStatus?.name.orEmpty(),
        event.detail
    ).joinToString("|") { encode(it) }

    fun decodeAuditEvent(value: String): QueueAuditEvent? {
        val fields = value.split("|").mapNotNull(::decode)
        if (fields.size != 11) return null
        val sequence = fields[1].toIntOrNull() ?: return null
        val action = runCatching { AuditAction.valueOf(fields[5]) }.getOrNull() ?: return null
        val fromStatus = fields[8].takeIf(String::isNotBlank)
            ?.let { runCatching { AppointmentStatus.valueOf(it) }.getOrNull() }
        val toStatus = fields[9].takeIf(String::isNotBlank)
            ?.let { runCatching { AppointmentStatus.valueOf(it) }.getOrNull() }
        return QueueAuditEvent(
            id = fields[0],
            sequence = sequence,
            date = fields[2],
            time = fields[3],
            actor = fields[4],
            action = action,
            token = fields[6].toIntOrNull(),
            patientName = fields[7].takeIf(String::isNotBlank),
            fromStatus = fromStatus,
            toStatus = toStatus,
            detail = fields[10]
        )
    }
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
