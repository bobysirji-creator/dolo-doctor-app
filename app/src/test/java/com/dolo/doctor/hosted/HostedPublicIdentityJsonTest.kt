package com.dolo.doctor.hosted

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class HostedPublicIdentityJsonTest {
    @Test fun parsesOnlyTheAuthenticatedDoctorIdentity() {
        val identity = HostedPublicIdentityJson.parse(payload("DLO-DOC-900001", "DOCTOR"), HostedStaffRole.DOCTOR)
        assertEquals("DLO-DOC-900001", identity.doloId)
        assertEquals(HostedStaffRole.DOCTOR, identity.role)
    }

    @Test fun rejectsCrossRoleOrInternalIdentityValues() {
        assertThrows(IllegalArgumentException::class.java) { HostedPublicIdentityJson.parse(payload("DLO-AST-900001", "ASSISTANT"), HostedStaffRole.DOCTOR) }
        assertThrows(IllegalArgumentException::class.java) { HostedPublicIdentityJson.parse(payload("00000000-0000-0000-0000-000000000017", "DOCTOR"), HostedStaffRole.DOCTOR) }
    }

    private fun payload(id:String,role:String) = """{"identity":{"doloId":"$id","displayName":"Demo Staff","role":"$role","prototype":true},"authoritative":true,"privacy":"SELF_ONLY_NO_PHONE","productionEnrollment":"DISABLED"}"""
}