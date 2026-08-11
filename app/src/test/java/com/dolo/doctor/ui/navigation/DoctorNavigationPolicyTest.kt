package com.dolo.doctor.ui.navigation

import com.dolo.doctor.data.model.Permission
import com.dolo.doctor.data.model.UserRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DoctorNavigationPolicyTest {
    @Test fun doctorSeesEveryMoreDestination() {
        assertEquals(
            DoctorMoreDestination.entries,
            DoctorNavigationPolicy.visibleMoreDestinations(UserRole.DOCTOR, emptySet(), null)
        )
    }

    @Test fun assistantSeesOnlyExplicitlyPermittedActions() {
        val visible = DoctorNavigationPolicy.visibleMoreDestinations(
            UserRole.ASSISTANT,
            setOf(Permission.VIEW_REPORTS, Permission.MANAGE_ANNOUNCEMENTS),
            "assistant-local"
        )
        assertTrue(DoctorMoreDestination.NOTIFICATIONS in visible)
        assertTrue(DoctorMoreDestination.CHANGE_PIN in visible)
        assertTrue(DoctorMoreDestination.REPORTS in visible)
        assertTrue(DoctorMoreDestination.ANNOUNCEMENTS in visible)
        assertFalse(DoctorMoreDestination.PROFILE in visible)
        assertFalse(DoctorMoreDestination.ASSISTANTS in visible)
        assertFalse(DoctorMoreDestination.BACKUP in visible)
    }

    @Test fun clinicAccessRemainsPermissionBound() {
        assertTrue(DoctorNavigationPolicy.canOpenClinic(UserRole.DOCTOR, emptySet()))
        assertTrue(DoctorNavigationPolicy.canOpenClinic(UserRole.ASSISTANT, setOf(Permission.VIEW_CLINIC)))
        assertTrue(DoctorNavigationPolicy.canOpenClinic(UserRole.ASSISTANT, setOf(Permission.MANAGE_CLINIC_AVAILABILITY)))
        assertFalse(DoctorNavigationPolicy.canOpenClinic(UserRole.ASSISTANT, emptySet()))
    }
}