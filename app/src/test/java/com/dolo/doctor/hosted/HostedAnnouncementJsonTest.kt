package com.dolo.doctor.hosted

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class HostedAnnouncementJsonTest {
    @Test
    fun parsesDoctorOwnedAnnouncements() {
        val announcements = HostedAnnouncementJson.parse(
            """{"announcements":[{"id":"50000000-0000-4000-8000-000000000001","clinicId":"20000000-0000-0000-0000-000000000016","kind":"DOCTOR_CAMP","title":"Sunday camp","message":"Screening at the clinic.","startsOn":"2026-07-21","endsOn":"2026-07-22","active":false}]}"""
        )

        assertEquals(1, announcements.size)
        assertEquals("DOCTOR_CAMP", announcements.single().kind)
        assertFalse(announcements.single().active)
    }
}