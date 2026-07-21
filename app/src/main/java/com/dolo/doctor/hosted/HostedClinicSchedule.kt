package com.dolo.doctor.hosted

import org.json.JSONArray
import org.json.JSONObject

data class HostedWeeklySession(
    val dayOfWeek: Int,
    val session: String,
    val startsAt: String,
    val endsAt: String,
    val maxTokens: Int,
    val averageConsultationMinutes: Int,
    val bookingEnabled: Boolean
)

data class HostedScheduleException(
    val serviceDate: String,
    val session: String?,
    val bookingEnabled: Boolean,
    val reason: String
)

data class HostedClinicSchedule(
    val clinicId: String,
    val ownerDoctorId: String,
    val futureBookingDays: Int,
    val rescheduleWindowDays: Int,
    val weeklySessions: List<HostedWeeklySession>,
    val exceptions: List<HostedScheduleException>
)

object HostedClinicScheduleJson {
    fun parse(json: String): HostedClinicSchedule {
        val schedule = JSONObject(json).getJSONObject("schedule")
        val weekly = schedule.getJSONArray("weeklySessions")
        val exceptions = schedule.getJSONArray("exceptions")
        return HostedClinicSchedule(
            clinicId = schedule.getString("clinicId"),
            ownerDoctorId = schedule.getString("ownerDoctorId"),
            futureBookingDays = schedule.getInt("futureBookingDays"),
            rescheduleWindowDays = schedule.getInt("rescheduleWindowDays"),
            weeklySessions = buildList {
                for (index in 0 until weekly.length()) {
                    val row = weekly.getJSONObject(index)
                    add(
                        HostedWeeklySession(
                            dayOfWeek = row.getInt("dayOfWeek"),
                            session = row.getString("session"),
                            startsAt = row.getString("startsAt").take(5),
                            endsAt = row.getString("endsAt").take(5),
                            maxTokens = row.getInt("maxTokens"),
                            averageConsultationMinutes = row.getInt("averageConsultationMinutes"),
                            bookingEnabled = row.getBoolean("bookingEnabled")
                        )
                    )
                }
            },
            exceptions = buildList {
                for (index in 0 until exceptions.length()) {
                    val row = exceptions.getJSONObject(index)
                    add(
                        HostedScheduleException(
                            serviceDate = row.getString("serviceDate"),
                            session = if (row.isNull("session")) null else row.getString("session"),
                            bookingEnabled = row.getBoolean("bookingEnabled"),
                            reason = row.optString("reason")
                        )
                    )
                }
            }
        )
    }

    fun scheduleBody(schedule: HostedClinicSchedule): String = JSONObject()
        .put("futureBookingDays", schedule.futureBookingDays)
        .put("rescheduleWindowDays", schedule.rescheduleWindowDays)
        .put(
            "weeklySessions",
            JSONArray().apply {
                schedule.weeklySessions.sortedWith(compareBy({ it.dayOfWeek }, { it.session })).forEach { row ->
                    put(
                        JSONObject()
                            .put("dayOfWeek", row.dayOfWeek)
                            .put("session", row.session)
                            .put("startsAt", row.startsAt)
                            .put("endsAt", row.endsAt)
                            .put("maxTokens", row.maxTokens)
                            .put("averageConsultationMinutes", row.averageConsultationMinutes)
                            .put("bookingEnabled", row.bookingEnabled)
                    )
                }
            }
        )
        .toString()

    fun exceptionBody(exception: HostedScheduleException): String = JSONObject()
        .put("serviceDate", exception.serviceDate)
        .put("session", exception.session ?: JSONObject.NULL)
        .put("bookingEnabled", exception.bookingEnabled)
        .put("reason", exception.reason.trim())
        .toString()
}
