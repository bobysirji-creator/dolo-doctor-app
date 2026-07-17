package com.dolo.doctor.data.model

import java.time.DayOfWeek
import java.time.LocalDate

enum class UserRole { DOCTOR, ASSISTANT }
enum class QueueState { NOT_STARTED, ACTIVE, PAUSED, CLOSED }
enum class AppointmentStatus { BOOKED, ARRIVED, WAITING, IN_CONSULTATION, COMPLETED, ABSENT, SKIPPED }
enum class AnnouncementType { AVAILABILITY, CAMP, OFFER, GENERAL }
enum class AnnouncementPublicationStatus { DRAFT, SCHEDULED, LIVE, EXPIRED }
enum class ProfileReviewStatus { VERIFIED, PENDING_REVIEW }
enum class BookingSource { PATIENT_APP, CLINIC_WALK_IN }
enum class PaymentStatus { PENDING, PAID, WAIVED }
enum class PaymentMethod { CASH, UPI, CARD, ONLINE, WAIVED }
// ONLINE remains only for backward decoding of older local records; current clinic UI does not offer it.
enum class AvailabilityImpactStatus { NONE, CONTACT_PENDING, PATIENT_NOTIFIED, RESCHEDULE_REQUIRED, RESOLVED }
enum class SyncStatus { LOCAL_ONLY, PENDING, SYNCED, CONFLICT, ERROR }
enum class WeeklyClosureScope { MORNING, EVENING, BOTH }
enum class AuditAction { QUEUE_STARTED, QUEUE_PAUSED, QUEUE_RESUMED, PATIENT_CALLED, STATUS_CHANGED, PATIENT_REJOINED, WALK_IN_BOOKED, FEE_CONFIRMED, RECEIPT_GENERATED, CONSULTATION_COMPLETED, SESSION_CLOSED, DAY_CLOSED, DAY_ROLLED_OVER, AVAILABILITY_SAVED, AVAILABILITY_CHANGED, AVAILABILITY_DELETED, AFFECTED_PATIENT_UPDATED, ANNOUNCEMENT_SAVED, ANNOUNCEMENT_VISIBILITY_CHANGED, ANNOUNCEMENT_DELETED, ASSISTANT_CREATED, ASSISTANT_STATUS_CHANGED, ASSISTANT_PERMISSIONS_CHANGED, ASSISTANT_PIN_RESET, ASSISTANT_DELETED, FEEDBACK_ACKNOWLEDGED, QUEUE_DELAY_NOTICE_SENT, SHARED_SYNC_PUBLISHED, SHARED_SYNC_PULLED, PATIENT_APP_BOOKING_RECEIVED, FUTURE_BOOKING_POLICY_CHANGED, WEEKLY_SCHEDULE_CHANGED }
enum class Permission {
    VIEW_QUEUE,
    UPDATE_QUEUE,
    CALL_NEXT_PATIENT,
    MARK_PATIENT_ARRIVED,
    MARK_PATIENT_ABSENT,
    MARK_PATIENT_COMPLETED,
    VIEW_TODAY_APPOINTMENTS,
    VIEW_CLINIC,
    VIEW_REPORTS,
    VIEW_PATIENT_FEEDBACK,
    SEND_QUEUE_DELAY_NOTICE,
    MANAGE_CLINIC_AVAILABILITY,
    MANAGE_ANNOUNCEMENTS,
    BOOK_WALK_IN_APPOINTMENT,
    GENERATE_TOKEN_RECEIPT,
    CONFIRM_CONSULTATION_FEE
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
    val averageConsultationMinutes: Int,
    val futureBookingEnabled: Boolean = false,
    val advanceBookingDays: Int = 7,
    val weeklyClosures: Map<DayOfWeek, WeeklyClosureScope> = emptyMap()
)

fun Clinic.isSessionClosed(date: LocalDate, session: String): Boolean {
    val scope = weeklyClosures[date.dayOfWeek] ?: return false
    return scope == WeeklyClosureScope.BOTH ||
        (scope == WeeklyClosureScope.MORNING && session == "Morning") ||
        (scope == WeeklyClosureScope.EVENING && session == "Evening")
}

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
    val receiptNumber: String = "",
    val consultationFee: Int = 0,
    val paymentStatus: PaymentStatus = PaymentStatus.PENDING,
    val paymentMethod: PaymentMethod? = null,
    val paidAt: String = "",
    val availabilityBlockId: String = "",
    val availabilityImpactStatus: AvailabilityImpactStatus = AvailabilityImpactStatus.NONE,
    val availabilityUpdatedAt: String = "",
    val lateQueuePlacement: Boolean = false,
    val lateArrivalAnchorToken: Int = 0
)

data class WalkInBookingRequest(
    val patientName: String,
    val patientPhone: String,
    val patientType: String,
    val session: String,
    val consultationFee: Int = 0,
    val paymentMethod: PaymentMethod = PaymentMethod.CASH
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
    val bookingSource: BookingSource,
    val consultationFee: Int,
    val paymentStatus: PaymentStatus,
    val paymentMethod: PaymentMethod,
    val paidAt: String
)

data class WalkInBookingResult(val receipt: TokenReceipt? = null, val error: String? = null)
data class FeeConfirmationResult(val receipt: TokenReceipt? = null, val error: String? = null)
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

data class AssistantCredentialIssue(
    val assistant: Assistant,
    val temporaryPin: String
)

data class AssistantCreationResult(
    val credential: AssistantCredentialIssue? = null,
    val error: String? = null
)

data class PatientFeedback(
    val id: String,
    val clinicId: String,
    val patientName: String,
    val rating: Int,
    val comment: String,
    val submittedOn: String,
    val acknowledged: Boolean = false
)

data class QueueDelayNotice(
    val id: String,
    val clinicId: String,
    val session: String,
    val delayMinutes: Int,
    val message: String,
    val createdOn: String,
    val createdAt: String,
    val createdBy: String
)

data class OperationalReport(
    val appointments: Int,
    val completed: Int,
    val absent: Int,
    val pending: Int,
    val collectedFees: Int,
    val averageRating: Double,
    val feedbackCount: Int,
    val clinicCount: Int
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

data class PatientProfileFeedItem(
    val announcementId: String,
    val doctorName: String,
    val clinicId: String,
    val title: String,
    val message: String,
    val type: AnnouncementType,
    val startsOn: String,
    val endsOn: String
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
    val currentToken: Int = 0,
    val selectedSession: String = "Morning",
    val notificationReadThrough: Int = 0,
    val feedback: List<PatientFeedback> = emptyList(),
    val queueDelayNotices: List<QueueDelayNotice> = emptyList(),
    val syncRevision: Long = 0,
    val syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY,
    val lastSyncedAt: String = "",
    val syncMessage: String = "Local offline data only"
)
