package com.dolo.doctor.hosted

import com.dolo.doctor.data.model.UserRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HostedRoleBoundaryTest {
    @Test
    fun localRoleMustMatchHostedRole() {
        assertTrue(HostedRoleBoundary.allows(UserRole.DOCTOR, HostedStaffRole.DOCTOR))
        assertTrue(HostedRoleBoundary.allows(UserRole.ASSISTANT, HostedStaffRole.ASSISTANT))
        assertFalse(HostedRoleBoundary.allows(UserRole.DOCTOR, HostedStaffRole.ASSISTANT))
        assertFalse(HostedRoleBoundary.allows(UserRole.ASSISTANT, HostedStaffRole.DOCTOR))
    }

    @Test
    fun expectedHostedRoleIsExplicit() {
        assertEquals(HostedStaffRole.DOCTOR, HostedRoleBoundary.expected(UserRole.DOCTOR))
        assertEquals(HostedStaffRole.ASSISTANT, HostedRoleBoundary.expected(UserRole.ASSISTANT))
    }
}