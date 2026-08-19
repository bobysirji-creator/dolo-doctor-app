package com.dolo.doctor.hosted

import org.junit.Assert.assertEquals
import org.junit.Test

class HostedStaffTokenJsonTest {
    @Test
    fun refreshResponseRetainsRoleWithoutSeededIdentityMetadata() {
        val payload = """{"accessToken":"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa","accessExpiresAt":"2026-08-19T10:15:00Z","refreshToken":"rrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrr","refreshExpiresAt":"2026-09-19T10:00:00Z"}"""
        val tokens = HostedStaffTokenJson.parseRefresh(payload, HostedStaffRole.DOCTOR)

        assertEquals(HostedStaffRole.DOCTOR, tokens.role)
        assertEquals(43, tokens.accessToken.length)
        assertEquals(43, tokens.refreshToken.length)
    }
}
