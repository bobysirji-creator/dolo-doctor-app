package com.dolo.doctor.integrations

import com.dolo.doctor.data.model.*

sealed interface ApiResult<out T> {
    data class Success<T>(val value: T) : ApiResult<T>
    data class Failure(val code: String, val message: String) : ApiResult<Nothing>
}

interface DoctorApi {
    suspend fun profile(): ApiResult<DoctorProfile>
    suspend fun clinics(): ApiResult<List<Clinic>>
    suspend fun todayAppointments(clinicId: String): ApiResult<List<Appointment>>
    suspend fun callNext(clinicId: String): ApiResult<Appointment>
    suspend fun updateAppointment(id: String, status: AppointmentStatus): ApiResult<Appointment>
    suspend fun bookWalkIn(request: WalkInBookingRequest): ApiResult<Appointment>
    suspend fun resumeSkippedConsultation(id: String): ApiResult<Appointment>
    suspend fun rejoinAppointment(id: String): ApiResult<Appointment>
    suspend fun announcements(): ApiResult<List<Announcement>>
    suspend fun availability(): ApiResult<List<AvailabilityBlock>>
    suspend fun assistants(): ApiResult<List<Assistant>>
}

interface AnnouncementPublisher {
    suspend fun publish(announcement: Announcement): ApiResult<Announcement>
    suspend fun deactivate(id: String): ApiResult<Unit>
}

interface AvailabilityManager {
    suspend fun block(block: AvailabilityBlock): ApiResult<AvailabilityBlock>
    suspend fun reopen(blockId: String): ApiResult<Unit>
}

interface DoctorNotificationService {
    suspend fun broadcastQueueDelay(clinicId: String, delayMinutes: Int): ApiResult<Unit>
    suspend fun notifyAffectedAppointments(blockId: String): ApiResult<Unit>
}
interface TokenReceiptGateway {
    suspend fun print(receipt: TokenReceipt): ApiResult<Unit>
}
