package com.dolo.doctor.integrations

import com.dolo.doctor.data.model.Announcement
import com.dolo.doctor.data.model.Appointment
import com.dolo.doctor.data.model.AppointmentStatus
import com.dolo.doctor.data.model.AvailabilityBlock
import com.dolo.doctor.data.model.BookingSource
import com.dolo.doctor.data.model.Clinic
import com.dolo.doctor.data.model.ConsultationQueue
import com.dolo.doctor.data.model.PaymentStatus
import com.dolo.doctor.data.model.QueueDelayNotice
import com.dolo.doctor.data.model.QueueState
import java.time.Instant

data class SharedClinicSnapshot(
    val revision: Long,
    val generatedAt: String,
    val clinic: Clinic,
    val queueDate: String,
    val queues: List<ConsultationQueue>,
    val appointments: List<Appointment>,
    val announcements: List<Announcement>,
    val availabilityBlocks: List<AvailabilityBlock>,
    val delayNotices: List<QueueDelayNotice>
)

data class PublishClinicCommand(
    val idempotencyKey: String,
    val baseRevision: Long,
    val snapshot: SharedClinicSnapshot
)

data class PatientBookingCommand(
    val idempotencyKey: String,
    val baseRevision: Long,
    val clinicId: String,
    val appointmentDate: String,
    val session: String,
    val patientName: String,
    val patientPhone: String,
    val patientType: String,
    val bookedAt: String
)

sealed interface SharedBackendResult<out T> {
    data class Success<T>(val value: T, val replayed: Boolean = false) : SharedBackendResult<T>
    data class Conflict(val serverRevision: Long, val message: String) : SharedBackendResult<Nothing>
    data class Failure(val message: String, val retryable: Boolean = true) : SharedBackendResult<Nothing>
}

interface SharedBackendGateway {
    fun publish(command: PublishClinicCommand): SharedBackendResult<SharedClinicSnapshot>
    fun pull(clinicId: String): SharedBackendResult<SharedClinicSnapshot>
    fun bookFromPatientApp(command: PatientBookingCommand): SharedBackendResult<SharedClinicSnapshot>
}

/**
 * Deterministic in-process transport for Stage 10 workflow testing.
 *
 * This is not a production server and never communicates between devices. It deliberately
 * implements the same revision, conflict and idempotency rules expected from the future HTTPS API.
 */
class LocalMockSharedBackendGateway(
    private val serverClock: () -> String = { Instant.now().toString() }
) : SharedBackendGateway {
    private var snapshot: SharedClinicSnapshot? = null
    private val publishResults = mutableMapOf<String, SharedClinicSnapshot>()
    private val bookingResults = mutableMapOf<String, SharedClinicSnapshot>()

    override fun publish(command: PublishClinicCommand): SharedBackendResult<SharedClinicSnapshot> {
        publishResults[command.idempotencyKey]?.let {
            return SharedBackendResult.Success(it, replayed = true)
        }
        val currentRevision = snapshot?.revision ?: command.baseRevision
        if (command.baseRevision != currentRevision) {
            return SharedBackendResult.Conflict(
                currentRevision,
                "Local revision ${command.baseRevision} is stale; pull revision $currentRevision before publishing."
            )
        }
        val accepted = command.snapshot.copy(
            revision = currentRevision + 1,
            generatedAt = serverClock()
        )
        snapshot = accepted
        publishResults[command.idempotencyKey] = accepted
        return SharedBackendResult.Success(accepted)
    }

    override fun pull(clinicId: String): SharedBackendResult<SharedClinicSnapshot> {
        val current = snapshot ?: return SharedBackendResult.Failure(
            "No shared snapshot exists yet. Publish the local clinic state first.",
            retryable = false
        )
        if (current.clinic.id != clinicId) {
            return SharedBackendResult.Failure("The requested clinic is not available in this mock transport.", false)
        }
        return SharedBackendResult.Success(current)
    }

    override fun bookFromPatientApp(command: PatientBookingCommand): SharedBackendResult<SharedClinicSnapshot> {
        bookingResults[command.idempotencyKey]?.let {
            return SharedBackendResult.Success(it, replayed = true)
        }
        val current = snapshot ?: return SharedBackendResult.Failure(
            "Publish the local clinic state before simulating a Patient App booking.",
            retryable = false
        )
        if (command.baseRevision != current.revision) {
            return SharedBackendResult.Conflict(
                current.revision,
                "Patient booking used stale revision ${command.baseRevision}; pull the latest snapshot."
            )
        }
        if (command.clinicId != current.clinic.id) {
            return SharedBackendResult.Failure("The booking does not match the active clinic.", false)
        }
        if (command.session !in setOf("Morning", "Evening")) {
            return SharedBackendResult.Failure("Select Morning or Evening.", false)
        }
        val policy = BookingPolicyEvaluator.evaluate(
            current.clinic,
            current.queueDate,
            command.appointmentDate,
            BookingSource.PATIENT_APP,
            command.session
        )
        if (!policy.allowed) return SharedBackendResult.Failure(policy.message, false)
        if (command.appointmentDate != current.queueDate) {
            return SharedBackendResult.Failure(
                "The policy allows this future date, but the Stage 11 local mock stores only the current clinic day. Use the hosted backend for scheduled appointments.",
                false
            )
        }
        val queue = current.queues.firstOrNull { it.session == command.session }
            ?: ConsultationQueue(command.session, QueueState.NOT_STARTED, 0)
        if (queue.state == QueueState.CLOSED) {
            return SharedBackendResult.Failure("${command.session} booking is closed.", false)
        }
        val sessionAppointments = current.appointments.filter { it.session == command.session }
        if (sessionAppointments.size >= current.clinic.maxTokensPerSession) {
            return SharedBackendResult.Failure("${command.session} has reached its token limit.", false)
        }
        val token = (sessionAppointments.maxOfOrNull { it.token } ?: 0) + 1
        val appointment = Appointment(
            id = "patient-" + command.idempotencyKey,
            token = token,
            patientName = command.patientName,
            patientType = command.patientType,
            session = command.session,
            status = AppointmentStatus.BOOKED,
            bookedAt = command.bookedAt,
            queueOrder = 0,
            bookingSource = BookingSource.PATIENT_APP,
            patientPhone = command.patientPhone,
            consultationFee = 0,
            paymentStatus = PaymentStatus.PENDING
        )
        val accepted = current.copy(
            revision = current.revision + 1,
            generatedAt = serverClock(),
            appointments = (current.appointments + appointment)
                .sortedWith(compareBy<Appointment> { it.session }.thenBy { it.token })
        )
        snapshot = accepted
        bookingResults[command.idempotencyKey] = accepted
        return SharedBackendResult.Success(accepted)
    }
}