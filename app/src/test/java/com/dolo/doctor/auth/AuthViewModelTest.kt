package com.dolo.doctor.auth

import com.dolo.doctor.data.model.UserRole
import com.dolo.doctor.data.model.Assistant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthViewModelTest {
    @Test fun restoredRepositorySessionBootstrapsAuthenticatedState() {
        val session = AuthSession(UserRole.DOCTOR, "doctor-1", "Dr. Aisha Mehta", "9999999999")
        val repository = FakeAuthRepository(session)
        val model = AuthViewModel(repository)

        assertEquals(session, model.uiState.session)
        model.logout()
        assertNull(model.uiState.session)
        assertNull(repository.restoredSession())
    }


    @Test fun mandatoryTemporaryPinCanBeReplaced() {
        val session = AuthSession(UserRole.ASSISTANT, "staff-new", "New Assistant", "9876509999", mustChangePin = true)
        val model = AuthViewModel(FakeAuthRepository(session))

        assertFalse(model.changePin("4826", "5678", "8765"))
        assertTrue(model.uiState.session?.mustChangePin == true)
        assertTrue(model.changePin("4826", "5678", "5678"))
        assertFalse(model.uiState.session?.mustChangePin == true)
    }

    private class FakeAuthRepository(private var session: AuthSession?) : AuthRepository {
        override fun restoredSession(): AuthSession? = session
        override fun login(role: UserRole, phone: String, pin: String): AuthResult = AuthResult.Failure("Not used")
        override fun changePin(session: AuthSession, currentPin: String, newPin: String): PinChangeResult {
            if (currentPin != "4826") return PinChangeResult.Failure("Incorrect")
            val updated = session.copy(mustChangePin = false)
            this.session = updated
            return PinChangeResult.Success(updated)
        }
        override fun logout() { session = null }
        override fun removedAssistantIds(): Set<String> = emptySet()
        override fun provisionAssistant(assistant: Assistant, temporaryPin: String): Boolean = false
        override fun setAssistantActive(assistant: Assistant): Boolean = false
        override fun resetAssistantPin(assistant: Assistant, temporaryPin: String): Boolean = false
        override fun removeAssistant(assistantId: String): Boolean = false
    }
}