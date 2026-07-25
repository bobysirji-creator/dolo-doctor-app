package com.dolo.doctor.hosted

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HostedDoctorCampaignJsonTest {
    @Test fun parsesAuthoritativeDoctorInAppCampaigns() {
        val campaigns = HostedDoctorCampaignJson.parse(
            """{"authoritative":true,"delivery":"IN_APP_ONLY","providers":"DISABLED","campaigns":[{"id":"campaign-37","messageType":"APP_UPDATE","title":"Doctor App update","message":"A new clinic tool is ready.","startsOn":"2026-07-25","endsOn":"2026-07-31"}]}"""
        )
        assertEquals(1, campaigns.size)
        assertEquals("APP_UPDATE", campaigns.single().messageType)
        assertEquals("Doctor App update", campaigns.single().title)
    }

    @Test fun rejectsProviderBackedDoctorCampaignPayloads() {
        val result = runCatching {
            HostedDoctorCampaignJson.parse(
                """{"authoritative":true,"delivery":"PUSH","providers":"ENABLED","campaigns":[]}"""
            )
        }
        assertTrue(result.isFailure)
    }
}
