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

    @Test fun sessionCodecSurvivesProcessReconstruction() {
        val session = AuthSession(UserRole.DOCTOR, "doctor-1", "Dr. Aisha Mehta", "9999999999")
        assertEquals(session, SessionCodec.decode(SessionCodec.encode(session)))
        assertNull(SessionCodec.decode("invalid-session"))
    }

    @Test fun assistantCredentialCodecAndSaltedHashRoundTrip() {
        val salt = PinHasher.newSalt()
        val record = AssistantCredentialRecord(
            "staff-new", "Anita Singh", "9876509999", true, salt, PinHasher.hash("4826", salt)
        )

        val restored = AssistantCredentialCodec.decode(AssistantCredentialCodec.encode(record))

        assertEquals(record, restored)
        assertTrue(PinHasher.matches("4826", salt, record.pinHash))
        assertFalse(PinHasher.matches("1234", salt, record.pinHash))
        assertFalse(record.pinHash.contains("4826"))
    }


    @Test fun temporaryPinFlagAndLegacyRecordsRemainCompatible() {
        val salt = PinHasher.newSalt()
        val temporary = AssistantCredentialRecord("staff-temp", "Temporary User", "9876508888", true, salt, PinHasher.hash("4826", salt), true)
        assertTrue(AssistantCredentialCodec.decode(AssistantCredentialCodec.encode(temporary))?.mustChangePin == true)
        val legacyFields = listOf("staff-old", "Old Assistant", "9876507777", "true", salt, PinHasher.hash("4826", salt))
        val legacyRecord = legacyFields.joinToString("|") {
            java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(it.toByteArray())
        }
        assertFalse(AssistantCredentialCodec.decode(legacyRecord)?.mustChangePin == true)

        val session = AuthSession(UserRole.ASSISTANT, "staff-temp", "Temporary User", "9876508888", true)
        assertEquals(session, SessionCodec.decode(SessionCodec.encode(session)))
        assertFalse(SessionCodec.decode("DOCTOR\tdoctor-1\tDoctor\t9999999999")?.mustChangePin == true)
    }

    @Test fun predictableReplacementPinsAreBlocked() {
        assertFalse(CredentialValidator.isAcceptableNewPin("1234"))
        assertFalse(CredentialValidator.isAcceptableNewPin("7777"))
        assertTrue(CredentialValidator.isAcceptableNewPin("4826"))
    }


    @Test fun legacyPinHashStillAuthenticatesForUpgrade() {
        val salt = PinHasher.newSalt()
        val legacy = java.security.MessageDigest.getInstance("SHA-256")
            .digest("$salt:4826".toByteArray(java.nio.charset.StandardCharsets.UTF_8))
            .let { java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(it) }

        assertTrue(PinHasher.matches("4826", salt, legacy))
        assertTrue(PinHasher.needsUpgrade(legacy))
        assertFalse(PinHasher.needsUpgrade(PinHasher.hash("4826", salt)))
    }    @Test fun malformedAssistantCredentialIsRejected() {
        assertNull(AssistantCredentialCodec.decode("not-a-credential"))
    }

    @Test fun demoCredentialsRespectSelectedRole() {
        assertEquals("doctor-1", DemoCredentials.authenticate(UserRole.DOCTOR, "9999999999", "1234")?.userId)
        assertEquals("staff-1", DemoCredentials.authenticate(UserRole.ASSISTANT, "9876543210", "1234")?.userId)
        assertNull(DemoCredentials.authenticate(UserRole.DOCTOR, "9876543210", "1234"))
        assertNull(DemoCredentials.authenticate(UserRole.ASSISTANT, "9876543210", "9999"))
    }
}