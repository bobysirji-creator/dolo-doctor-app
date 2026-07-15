package com.dolo.doctor.data.model

enum class UserRole { DOCTOR, ASSISTANT }
enum class QueueState { NOT_STARTED, ACTIVE, PAUSED, CLOSED }
enum class AppointmentStatus { BOOKED, ARRIVED, WAITING, IN_CONSULTATION, COMPLETED, ABSENT, SKIPPED }
enum class AnnouncementType { AVAILABILITY, CAMP, OFFER, GENERAL }
enum class ProfileReviewStatus { VERIFIED, PENDING_REVIEW }
enum class BookingSource { PATIENT_APP, CLINIC_WALK_IN }
enum class AuditAction { QUEUE_STARTED, QUEUE_PAUSED, QUEUE_RESUMED, PATIENT_CALLED, STATUS_CHANGED, PATIENT_REJOINED, WALK_IN_BOOKED, RECEIPT_GENERATED, CONSULTATION_COMPLETED, DAY_CLOSED, DAY_ROLLED_OVER }
enum class Permission {
    VIEW_QUEUE,
    UPDATE_QUEUE,
    CALL_NEXT_PATIENT,
    MARK_PATIENT_ARRIVED,
    MARK_PATIENT_ABSENT,
    MARK_PATIENT_COMPLETED,
    VIEW_TODAY_APPOINTMENTS,
    MANAGE_CLINIC_AVAILABILITY,
    MANAGE_ANNOUNCEMENTS,
    BOOK_WALK_IN_APPOINTMENT,
    GENERATE_TOKEN_RECEIPT
}

data class DoctorProfile(
    val name: String,
    val specialty: String,
    val qualification: String,
    val registrationNumber: String,
    val experienceYears: Int,
    val consultationFee: Int,
    val about: String,
    val verified: Boolean,
    val reviewStatus: ProfileReviewStatus = ProfileReviewStatus.VERIFIED
)

data class Clinic(
    val id: String,
    val name: String,
    val address: String,
    val phone: String,
    val morningSession: String,
    val eveningSession: String,
    val maxTokensPerSession: Int,
    val averageConsultationMinutes: Int
)

data class ConsultationQueue(
    val session: String,
    val state: QueueState,
    val currentToken: Int
)
data class Appointment(
    val id: String,
    val token: Int,
    val patientName: String,
    val patientType: String,
    val session: String,
    val status: AppointmentStatus,
    val bookedAt: String,
    val queueOrder: Int = token,
    val bookingSource: BookingSource = BookingSource.PATIENT_APP,
    val patientPhone: String = "",
    val receiptNumber: String = ""
)

data class WalkInBookingRequest(
    val patientName: String,
    val patientPhone: String,
    val patientType: String,
    val session: String
)

data class TokenReceipt(
    val receiptNumber: String,
    val token: Int,
    val appointmentDate: String,
    val generatedAt: String,
    val patientName: String,
    val patientPhone: String,
    val patientType: String,
    val doctorName: String,
    val clinicName: String,
    val clinicAddress: String,
    val session: String,
    val bookingSource: BookingSource
)

data class WalkInBookingResult(val receipt: TokenReceipt? = null, val error: String? = null)
data class DailyQueueHistory(
    val date: String,
    val clinicName: String,
    val closedAt: String,
    val closureReason: String,
    val finalToken: Int,
    val appointments: List<Appointment>
)


data class QueueAuditEvent(
    val id: String,
    val sequence: Int,
    val date: String,
    val time: String,
    val actor: String,
    val action: AuditAction,
    val token: Int? = null,
    val patientName: String? = null,
    val fromStatus: AppointmentStatus? = null,
    val toStatus: AppointmentStatus? = null,
    val detail: String
)
data class Assistant(
    val id: String,
    val name: String,
    val phone: String,
    val active: Boolean,
    val permissions: Set<Permission>
)

data class Announcement(
    val id: String,
    val title: String,
    val message: String,
    val type: AnnouncementType,
    val startsOn: String,
    val endsOn: String,
    val active: Boolean
)

data class AvailabilityBlock(
    val id: String,
    val clinicId: String,
    val fromDate: String,
    val toDate: String,
    val sessions: String,
    val reason: String,
    val appointmentsEnabled: Boolean
)

data class DoctorUiState(
    val role: UserRole? = null,
    val activeAssistantId: String? = null,
    val profile: DoctorProfile,
    val clinics: List<Clinic>,
    val appointments: List<Appointment>,
    val assistants: List<Assistant>,
    val announcements: List<Announcement>,
    val availabilityBlocks: List<AvailabilityBlock>,
    val queueDate: String,
    val queueHistory: List<DailyQueueHistory> = emptyList(),
    val auditEvents: List<QueueAuditEvent> = emptyList(),
    val sessionQueues: List<ConsultationQueue> = emptyList(),
    val queueState: QueueState = QueueState.NOT_STARTED,
    val currentToken: Int = 0
)
