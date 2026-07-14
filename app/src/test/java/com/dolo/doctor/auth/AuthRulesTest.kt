package com.dolo.doctor.auth

import com.dolo.doctor.data.model.UserRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthRulesTest {
    @Test fun phoneAndPinValidationAreStrict() {
        assertTrue(CredentialValidator.isValidPhone("98765 43210"))
        assertEquals("9876543210", CredentialValidator.normalizePhone("+91 98765 43210"))
        assertFalse(CredentialValidator.isValidPhone("12345"))
        assertTrue(CredentialValidator.isValidPin("1234"))
        assertFalse(CredentialValidator.isValidPin("12a4"))
    }

    @Test fun demoCredentialsRespectSelectedRole() {
        assertEquals("doctor-1", DemoCredentials.authenticate(UserRole.DOCTOR, "9999999999", "1234")?.userId)
        assertEquals("staff-1", DemoCredentials.authenticate(UserRole.ASSISTANT, "9876543210", "1234")?.userId)
        assertNull(DemoCredentials.authenticate(UserRole.DOCTOR, "9876543210", "1234"))
        assertNull(DemoCredentials.authenticate(UserRole.ASSISTANT, "9876543210", "9999"))
    }
}