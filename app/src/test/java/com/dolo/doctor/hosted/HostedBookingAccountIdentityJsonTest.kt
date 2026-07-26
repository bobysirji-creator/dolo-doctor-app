package com.dolo.doctor.hosted

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class HostedBookingAccountIdentityJsonTest {
    @Test
    fun `family appointment keeps booking account ownership explicit`() {
        val item = JSONObject()
            .put("patientRelationship", "FAMILY")
            .put("bookingAccountDoloId", "DLO-PAT-000001")

        assertEquals("FAMILY", HostedBookingAccountIdentityJson.relationship(item))
        assertEquals("DLO-PAT-000001", HostedBookingAccountIdentityJson.doloId(item))
        assertEquals(
            "Booking account: DLO-PAT-000001 | Family appointment",
            HostedBookingAccountIdentityJson.label("FAMILY", "DLO-PAT-000001")
        )
    }

    @Test
    fun `rejects cross role and malformed public identity`() {
        assertThrows(IllegalArgumentException::class.java) {
            HostedBookingAccountIdentityJson.doloId(
                JSONObject().put("bookingAccountDoloId", "DLO-DOC-000001")
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            HostedBookingAccountIdentityJson.relationship(
                JSONObject().put("patientRelationship", "DEPENDENT")
            )
        }
    }
}