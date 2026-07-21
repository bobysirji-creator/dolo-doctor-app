package com.dolo.doctor.hosted

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HostedClinicScheduleJsonTest {
    @Test
    fun parsesWeeklyRulesAndDateExceptions() {
        val schedule = HostedClinicScheduleJson.parse(
            """{"schedule":{"clinicId":"clinic-1","ownerDoctorId":"doctor-1","futureBookingDays":7,"rescheduleWindowDays":10,"weeklySessions":[{"dayOfWeek":1,"session":"MORNING","startsAt":"09:00:00","endsAt":"12:00:00","maxTokens":20,"averageConsultationMinutes":12,"bookingEnabled":true}],"exceptions":[{"serviceDate":"2026-07-26","session":null,"bookingEnabled":false,"reason":"Sunday off"}]}}"""
        )

        assertEquals(7, schedule.futureBookingDays)
        assertEquals("09:00", schedule.weeklySessions.single().startsAt)
        assertEquals(20, schedule.weeklySessions.single().maxTokens)
        assertNull(schedule.exceptions.single().session)
    }

    @Test
    fun writesBoundedScheduleAndWholeDayExceptionBodies() {
        val schedule = HostedClinicSchedule("clinic-1", "doctor-1", 0, 10, listOf(HostedWeeklySession(0, "EVENING", "17:00", "20:00", 15, 12, false)), emptyList())
        val body = JSONObject(HostedClinicScheduleJson.scheduleBody(schedule))
        assertEquals(setOf("futureBookingDays", "rescheduleWindowDays", "weeklySessions"), body.keys().asSequence().toSet())
        assertEquals(false, body.getJSONArray("weeklySessions").getJSONObject(0).getBoolean("bookingEnabled"))

        val exception = JSONObject(HostedClinicScheduleJson.exceptionBody(HostedScheduleException("2026-07-27", null, false, "Clinic closed")))
        assertEquals(true, exception.isNull("session"))
        assertEquals("Clinic closed", exception.getString("reason"))
    }
}
