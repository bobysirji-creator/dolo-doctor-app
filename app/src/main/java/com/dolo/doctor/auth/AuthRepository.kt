package com.dolo.doctor.auth

import android.content.SharedPreferences
import com.dolo.doctor.data.model.UserRole

data class AuthSession(val role: UserRole, val userId: String, val displayName: String, val phone: String)

sealed interface AuthResult {
    data class Success(val session: AuthSession) : AuthResult
    data class Failure(val message: String) : AuthResult
}

object CredentialValidator {
    fun normalizePhone(value: String): String = value.filter(Char::isDigit).takeLast(10)
    fun isValidPhone(value: String): Boolean = normalizePhone(value).length == 10
    fun isValidPin(value: String): Boolean = value.length == 4 && value.all(Char::isDigit)
}

object DemoCredentials {
    private val sessions = listOf(
        AuthSession(UserRole.DOCTOR, "doctor-1", "Dr. Aisha Mehta", "9999999999"),
        AuthSession(UserRole.ASSISTANT, "staff-1", "Neha Kapoor", "9876543210"),
        AuthSession(UserRole.ASSISTANT, "staff-2", "Ravi Kumar", "9876501234")
    )

    fun authenticate(role: UserRole, phone: String, pin: String): AuthSession? {
        if (pin != "1234") return null
        val normalized = CredentialValidator.normalizePhone(phone)
        return sessions.firstOrNull { it.role == role && it.phone == normalized }
    }
}

interface AuthRepository {
    fun restoredSession(): AuthSession?
    fun login(role: UserRole, phone: String, pin: String): AuthResult
    fun logout()
    fun removedAssistantIds(): Set<String>
    fun removeAssistant(assistantId: String): Boolean
}

class LocalAuthRepository(private val preferences: SharedPreferences) : AuthRepository {
    override fun restoredSession(): AuthSession? {
        val role = preferences.getString(KEY_ROLE, null)?.let { runCatching { UserRole.valueOf(it) }.getOrNull() } ?: return null
        val userId = preferences.getString(KEY_USER_ID, null) ?: return null
        val displayName = preferences.getString(KEY_DISPLAY_NAME, null) ?: return null
        val phone = preferences.getString(KEY_PHONE, null) ?: return null
        return AuthSession(role, userId, displayName, phone)
    }

    override fun login(role: UserRole, phone: String, pin: String): AuthResult {
        if (!CredentialValidator.isValidPhone(phone)) return AuthResult.Failure("Enter a valid 10-digit mobile number.")
        if (!CredentialValidator.isValidPin(pin)) return AuthResult.Failure("Enter the 4-digit demo PIN.")
        val session = DemoCredentials.authenticate(role, phone, pin) ?: return AuthResult.Failure("Credentials do not match the selected role.")
        if (session.role == UserRole.ASSISTANT && session.userId in removedAssistantIds()) return AuthResult.Failure("This assistant account has been removed by the doctor.")
        val saved = preferences.edit()
            .putString(KEY_ROLE, session.role.name)
            .putString(KEY_USER_ID, session.userId)
            .putString(KEY_DISPLAY_NAME, session.displayName)
            .putString(KEY_PHONE, session.phone)
            .commit()
        return if (saved) AuthResult.Success(session) else AuthResult.Failure("Unable to save this session. Please try again.")
    }

    override fun logout() {
        preferences.edit().remove(KEY_ROLE).remove(KEY_USER_ID).remove(KEY_DISPLAY_NAME).remove(KEY_PHONE).commit()
    }

    override fun removedAssistantIds(): Set<String> = preferences.getStringSet(KEY_REMOVED_ASSISTANTS, emptySet()).orEmpty().toSet()

    override fun removeAssistant(assistantId: String): Boolean {
        val updated = removedAssistantIds() + assistantId
        return preferences.edit().putStringSet(KEY_REMOVED_ASSISTANTS, updated).commit()
    }

    private companion object {
        const val KEY_ROLE = "auth_role"
        const val KEY_USER_ID = "auth_user_id"
        const val KEY_DISPLAY_NAME = "auth_display_name"
        const val KEY_PHONE = "auth_phone"
        const val KEY_REMOVED_ASSISTANTS = "removed_assistant_ids"
    }
}