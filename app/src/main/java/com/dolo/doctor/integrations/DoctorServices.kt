package com.dolo.doctor.integrations

import com.dolo.doctor.data.model.*

sealed interface ApiResult<out T> {
    data class Success<T>(val value: T) : ApiResult<T>
    data class Failure(val code: String, val message: String) : ApiResult<Nothing>
}

interface DoctorApi {
    suspend fun profile(): ApiResult<DoctorProfile>
    suspend fun clinics(): ApiResult<List<Clinic>>
    suspend fun todayAppointments(clinicId: String, session: String? = null): ApiResult<List<Appointment>>
    suspend fun callNext(clinicId: String, session: String): ApiResult<Appointment>
    suspend fun updateAppointment(id: String, status: AppointmentStatus): ApiResult<Appointment>
    suspend fun bookWalkIn(request: WalkInBookingRequest): ApiResult<Appointment>
    suspend fun confirmConsultationFee(appointmentId: String, amount: Int, method: PaymentMethod): ApiResult<Appointment>
    suspend fun resumeSkippedConsultation(id: String): ApiResult<Appointment>
    suspend fun rejoinAppointment(id: String): ApiResult<Appointment>
    suspend fun announcements(): ApiResult<List<Announcement>>
    suspend fun availability(): ApiResult<List<AvailabilityBlock>>
    suspend fun assistants(): ApiResult<List<Assistant>>
}

interface AnnouncementPublisher {
    suspend fun save(announcement: Announcement): ApiResult<Announcement>
    suspend fun setActive(id: String, active: Boolean): ApiResult<Announcement>
    suspend fun delete(id: String): ApiResult<Unit>
}

interface PatientProfileFeedGateway {
    suspend fun doctorUpdates(doctorId: String, clinicId: String?, onDate: String): ApiResult<List<PatientProfileFeedItem>>
}


interface AvailabilityManager {
    suspend fun save(block: AvailabilityBlock): ApiResult<AvailabilityBlock>
    suspend fun setAppointmentsEnabled(blockId: String, enabled: Boolean): ApiResult<AvailabilityBlock>
    suspend fun delete(blockId: String): ApiResult<Unit>
    suspend fun updateAffectedPatient(appointmentId: String, status: AvailabilityImpactStatus): ApiResult<Appointment>
}

interface AssistantAdminGateway {
    suspend fun create(name: String, phone: String, permissions: Set<Permission>): ApiResult<Assistant>
    suspend fun setActive(assistantId: String, active: Boolean): ApiResult<Assistant>
    suspend fun setPermissions(assistantId: String, permissions: Set<Permission>): ApiResult<Assistant>
    suspend fun resetTemporaryPin(assistantId: String): ApiResult<Unit>
    suspend fun delete(assistantId: String): ApiResult<Unit>
}

interface OperationalReportGateway {
    suspend fun report(clinicId: String?, fromDate: String, toDate: String): ApiResult<OperationalReport>
}

interface PatientFeedbackGateway {
    suspend fun feedback(clinicId: String?): ApiResult<List<PatientFeedback>>
    suspend fun acknowledge(feedbackId: String): ApiResult<PatientFeedback>
}

interface QueueDelayNoticeGateway {
    suspend fun create(notice: QueueDelayNotice): ApiResult<QueueDelayNotice>
    suspend fun activeNotices(clinicId: String, onDate: String): ApiResult<List<QueueDelayNotice>>
}

interface ClinicPortfolioGateway {
    suspend fun clinics(): ApiResult<List<Clinic>>
    suspend fun switchOperationalClinic(clinicId: String): ApiResult<Clinic>
}

interface DoctorNotificationService {
    suspend fun broadcastQueueDelay(clinicId: String, delayMinutes: Int): ApiResult<Unit>
    suspend fun notifyAffectedAppointments(blockId: String): ApiResult<Unit>
}
interface TokenReceiptGateway {
    suspend fun print(receipt: TokenReceipt): ApiResult<Unit>
}
