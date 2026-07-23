package com.dolo.doctor.hosted

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class HostedStaffNotificationJsonTest {
    @Test
    fun parsesAuthoritativeClinicNotificationPage() {
        val notifications = HostedStaffNotificationJson.parse(
            """
            {
              "authoritative": true,
              "notifications": [{
                "cursor": "42",
                "appointmentId": "appointment-1",
                "clinicId": "clinic-1",
                "patientName": "Family Patient",
                "tokenNumber": 7,
                "kind": "ONLINE_BOOKING_CREATED",
                "title": "New online appointment",
                "message": "Token 7 was booked for Family Patient.",
                "occurredAt": "2026-07-23T06:30:00.000Z",
                "read": false
              }]
            }
            """.trimIndent()
        )

        assertEquals(1, notifications.size)
        assertEquals("42", notifications.single().cursor)
        assertEquals("Family Patient", notifications.single().patientName)
        assertEquals(7, notifications.single().tokenNumber)
        assertFalse(notifications.single().read)
    }
}