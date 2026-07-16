package com.dolo.doctor.auth

import android.content.SharedPreferences
import com.dolo.doctor.data.model.Assistant
import com.dolo.doctor.data.model.UserRole
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

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

internal data class AssistantCredentialRecord(
    val assistantId: String,
    val displayName: String,
    val phone: String,
    val active: Boolean,
    val salt: String,
    val pinHash: String
)

internal object AssistantCredentialCodec {
    private val encoder = Base64.getUrlEncoder().withoutPadding()
    private val decoder = Base64.getUrlDecoder()

    fun encode(record: AssistantCredentialRecord): String = listOf(
        record.assistantId,
        record.displayName,
        record.phone,
        record.active.toString(),
        record.salt,
        record.pinHash
    ).joinToString("|") { encoder.encodeToString(it.toByteArray(StandardCharsets.UTF_8)) }

    fun decode(value: String): AssistantCredentialRecord? {
        val fields = value.split("|").mapNotNull { field ->
            runCatching { String(decoder.decode(field), StandardCharsets.UTF_8) }.getOrNull()
        }
        if (fields.size != 6) return null
        val active = fields[3].toBooleanStrictOrNull() ?: return null
        if (!CredentialValidator.isValidPhone(fields[2])) return null
        return AssistantCredentialRecord(fields[0], fields[1], CredentialValidator.normalizePhone(fields[2]), active, fields[4], fields[5])
    }
}

internal object PinHasher {
    private val encoder = Base64.getUrlEncoder().withoutPadding()

    fun newSalt(): String = ByteArray(16).also(SecureRandom()::nextBytes)
        .let(encoder::encodeToString)

    fun hash(pin: String, salt: String): String = MessageDigest.getInstance("SHA-256")
        .digest("$salt:$pin".toByteArray(StandardCharsets.UTF_8))
        .let(encoder::encodeToString)

    fun matches(pin: String, salt: String, expectedHash: String): Boolean =
        MessageDigest.isEqual(
            hash(pin, salt).toByteArray(StandardCharsets.UTF_8),
            expectedHash.toByteArray(StandardCharsets.UTF_8)
        )
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
    fun provisionAssistant(assistant: Assistant, temporaryPin: String): Boolean
    fun setAssistantActive(assistant: Assistant): Boolean
    fun resetAssistantPin(assistant: Assistant, temporaryPin: String): Boolean
    fun removeAssistant(assistantId: String): Boolean
}

class LocalAuthRepository(
    private val preferences: SharedPreferences,
    private val sessionFileStore: SessionFileStore,
    private val durablePreferences: SharedPreferences
) : AuthRepository {
    override fun restoredSession(): AuthSession? {
        val durableSession = durablePreferences.getString(KEY_DURABLE_SESSION, null)?.let(SessionCodec::decode)
        if (durableSession != null) return restoreIfAllowed(durableSession) {
            sessionFileStore.write(durableSession)
        }
        val fileSession = sessionFileStore.read()
        if (fileSession != null) return restoreIfAllowed(fileSession) {
            durablePreferences.edit().putString(KEY_DURABLE_SESSION, SessionCodec.encode(fileSession)).commit()
        }
        val role = preferences.getString(KEY_ROLE, null)?.let { runCatching { UserRole.valueOf(it) }.getOrNull() } ?: return null
        val userId = preferences.getString(KEY_USER_ID, null) ?: return null
        val displayName = preferences.getString(KEY_DISPLAY_NAME, null) ?: return null
        val phone = preferences.getString(KEY_PHONE, null) ?: return null
        val session = AuthSession(role, userId, displayName, phone)
        return restoreIfAllowed(session) {
            sessionFileStore.write(session)
            durablePreferences.edit().putString(KEY_DURABLE_SESSION, SessionCodec.encode(session)).commit()
        }
    }

    private fun restoreIfAllowed(session: AuthSession, migrate: () -> Unit): AuthSession? {
        if (!isSessionAllowed(session)) {
            logout()
            return null
        }
        migrate()
        return session
    }

    private fun isSessionAllowed(session: AuthSession): Boolean {
        if (session.role != UserRole.ASSISTANT) return true
        if (session.userId in removedAssistantIds()) return false
        return credentialRecords().firstOrNull { it.assistantId == session.userId }?.active ?: true
    }

    override fun login(role: UserRole, phone: String, pin: String): AuthResult {
        if (!CredentialValidator.isValidPhone(phone)) return AuthResult.Failure("Enter a valid 10-digit mobile number.")
        if (!CredentialValidator.isValidPin(pin)) return AuthResult.Failure("Enter the 4-digit PIN.")
        val normalized = CredentialValidator.normalizePhone(phone)
        val session = when (role) {
            UserRole.DOCTOR -> DemoCredentials.authenticate(role, normalized, pin)
            UserRole.ASSISTANT -> authenticateAssistant(normalized, pin)
        } ?: return AuthResult.Failure("Credentials do not match this role, or this account is disabled.")
        if (session.role == UserRole.ASSISTANT && session.userId in removedAssistantIds()) {
            return AuthResult.Failure("This assistant account has been removed by the doctor.")
        }
        val preferencesSaved = preferences.edit()
            .putString(KEY_ROLE, session.role.name)
            .putString(KEY_USER_ID, session.userId)
            .putString(KEY_DISPLAY_NAME, session.displayName)
            .putString(KEY_PHONE, session.phone)
            .commit()
        val fileSaved = sessionFileStore.write(session)
        val durableSaved = durablePreferences.edit().putString(KEY_DURABLE_SESSION, SessionCodec.encode(session)).commit()
        return if (durableSaved || preferencesSaved || fileSaved) AuthResult.Success(session) else AuthResult.Failure("Unable to save this session. Please try again.")
    }

    private fun authenticateAssistant(phone: String, pin: String): AuthSession? {
        val stored = credentialRecords().firstOrNull { it.phone == phone }
        if (stored != null) {
            if (!stored.active || !PinHasher.matches(pin, stored.salt, stored.pinHash)) return null
            return AuthSession(UserRole.ASSISTANT, stored.assistantId, stored.displayName, stored.phone)
        }
        return DemoCredentials.authenticate(UserRole.ASSISTANT, phone, pin)
    }

    override fun logout() {
        preferences.edit().remove(KEY_ROLE).remove(KEY_USER_ID).remove(KEY_DISPLAY_NAME).remove(KEY_PHONE).commit()
        sessionFileStore.clear()
        durablePreferences.edit().remove(KEY_DURABLE_SESSION).commit()
    }

    override fun removedAssistantIds(): Set<String> = preferences.getStringSet(KEY_REMOVED_ASSISTANTS, emptySet()).orEmpty().toSet()

    override fun provisionAssistant(assistant: Assistant, temporaryPin: String): Boolean {
        if (!CredentialValidator.isValidPin(temporaryPin) || !CredentialValidator.isValidPhone(assistant.phone)) return false
        val salt = PinHasher.newSalt()
        val record = AssistantCredentialRecord(
            assistant.id,
            assistant.name,
            CredentialValidator.normalizePhone(assistant.phone),
            assistant.active,
            salt,
            PinHasher.hash(temporaryPin, salt)
        )
        return saveCredential(record)
    }

    override fun setAssistantActive(assistant: Assistant): Boolean {
        val existing = credentialRecords().firstOrNull { it.assistantId == assistant.id }
        if (existing == null) return provisionAssistant(assistant, LEGACY_ASSISTANT_PIN)
        return saveCredential(existing.copy(displayName = assistant.name, phone = assistant.phone, active = assistant.active))
    }

    override fun resetAssistantPin(assistant: Assistant, temporaryPin: String): Boolean =
        provisionAssistant(assistant, temporaryPin)

    override fun removeAssistant(assistantId: String): Boolean {
        val updatedRemoved = removedAssistantIds() + assistantId
        val remainingCredentials = credentialRecords().filterNot { it.assistantId == assistantId }
            .mapTo(mutableSetOf(), AssistantCredentialCodec::encode)
        return preferences.edit()
            .putStringSet(KEY_REMOVED_ASSISTANTS, updatedRemoved)
            .putStringSet(KEY_ASSISTANT_CREDENTIALS, remainingCredentials)
            .commit()
    }

    private fun credentialRecords(): List<AssistantCredentialRecord> =
        preferences.getStringSet(KEY_ASSISTANT_CREDENTIALS, emptySet()).orEmpty()
            .mapNotNull(AssistantCredentialCodec::decode)

    private fun saveCredential(record: AssistantCredentialRecord): Boolean {
        val records = credentialRecords().filterNot {
            it.assistantId == record.assistantId || it.phone == record.phone
        } + record
        return preferences.edit().putStringSet(
            KEY_ASSISTANT_CREDENTIALS,
            records.mapTo(mutableSetOf(), AssistantCredentialCodec::encode)
        ).commit()
    }

    private companion object {
        const val KEY_ROLE = "auth_role"
        const val KEY_USER_ID = "auth_user_id"
        const val KEY_DISPLAY_NAME = "auth_display_name"
        const val KEY_PHONE = "auth_phone"
        const val KEY_REMOVED_ASSISTANTS = "removed_assistant_ids"
        const val KEY_ASSISTANT_CREDENTIALS = "assistant_credentials_v1"
        const val KEY_DURABLE_SESSION = "authenticated_session_v1"
        const val LEGACY_ASSISTANT_PIN = "1234"
    }
}
