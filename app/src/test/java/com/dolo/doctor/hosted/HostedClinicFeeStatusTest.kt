package com.dolo.doctor.hosted

import org.junit.Assert.assertEquals
import org.junit.Test

class HostedClinicFeeStatusTest {
    @Test
    fun `hosted admission exposes only server-supported clinic fee states`() {
        assertEquals(listOf("PAID", "WAIVED"), HostedClinicFeeStatus.entries.map { it.name })
    }
}