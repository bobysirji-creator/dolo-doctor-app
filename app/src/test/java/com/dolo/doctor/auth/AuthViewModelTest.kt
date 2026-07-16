package com.dolo.doctor.auth

import com.dolo.doctor.data.model.UserRole
import com.dolo.doctor.data.model.Assistant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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

    private class FakeAuthRepository(private var session: AuthSession?) : AuthRepository {
        override fun restoredSession(): AuthSession? = session
        override fun login(role: UserRole, phone: String, pin: String): AuthResult = AuthResult.Failure("Not used")
        override fun logout() { session = null }
        override fun removedAssistantIds(): Set<String> = emptySet()
        override fun provisionAssistant(assistant: Assistant, temporaryPin: String): Boolean = false
        override fun setAssistantActive(assistant: Assistant): Boolean = false
        override fun resetAssistantPin(assistant: Assistant, temporaryPin: String): Boolean = false
        override fun removeAssistant(assistantId: String): Boolean = false
    }
}