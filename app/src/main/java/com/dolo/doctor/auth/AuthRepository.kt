package com.dolo.doctor.auth

import android.content.SharedPreferences
import com.dolo.doctor.data.model.Assistant
import com.dolo.doctor.data.model.UserRole
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

data class AuthSession(val role: UserRole, val userId: String, val displayName: String, val phone: String, val mustChangePin: Boolean = false)

sealed interface AuthResult {
    data class Success(val session: AuthSession) : AuthResult
    data class Failure(val message: String) : AuthResult
}

object CredentialValidator {
    fun normalizePhone(value: String): String = value.filter(Char::isDigit).takeLast(10)
    fun isValidPhone(value: String): Boolean = normalizePhone(value).length == 10
    fun isValidPin(value: String): Boolean = value.length == 4 && value.all(Char::isDigit)
    fun isAcceptableNewPin(value: String): Boolean = isValidPin(value) && value !in setOf("0000", "1111", "1234", "2222", "3333", "4444", "5555", "6666", "7777", "8888", "9999", "4321")
}

internal data class AssistantCredentialRecord(
    val assistantId: String,
    val displayName: String,
    val phone: String,
    val active: Boolean,
    val salt: String,
    val pinHash: String,
    val mustChangePin: Boolean = false
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
        record.pinHash,
        record.mustChangePin.toString()
    ).joinToString("|") { encoder.encodeToString(it.toByteArray(StandardCharsets.UTF_8)) }

    fun decode(value: String): AssistantCredentialRecord? {
        val fields = value.split("|").mapNotNull { field ->
            runCatching { String(decoder.decode(field), StandardCharsets.UTF_8) }.getOrNull()
        }
        if (fields.size !in setOf(6, 7)) return null
        val active = fields[3].toBooleanStrictOrNull() ?: return null
        if (!CredentialValidator.isValidPhone(fields[2])) return null
        val mustChangePin = if (fields.size == 7) fields[6].toBooleanStrictOrNull() ?: return null else false
        return AssistantCredentialRecord(fields[0], fields[1], CredentialValidator.normalizePhone(fields[2]), active, fields[4], fields[5], mustChangePin)
    }
}

internal object PinHasher {
    private val encoder = Base64.getUrlEncoder().withoutPadding()
    private val decoder = Base64.getUrlDecoder()
    private const val PREFIX = "pbkdf2-sha256:100000:"

    fun newSalt(): String = ByteArray(16).also(SecureRandom()::nextBytes).let(encoder::encodeToString)

    fun hash(pin: String, salt: String): String {
        val spec = PBEKeySpec(pin.toCharArray(), decoder.decode(salt), 100_000, 256)
        return try {
            PREFIX + encoder.encodeToString(SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded)
        } finally {
            spec.clearPassword()
        }
    }

    fun matches(pin: String, salt: String, expectedHash: String): Boolean {
        val candidate = if (expectedHash.startsWith(PREFIX)) hash(pin, salt) else legacyHash(pin, salt)
        return MessageDigest.isEqual(candidate.toByteArray(StandardCharsets.UTF_8), expectedHash.toByteArray(StandardCharsets.UTF_8))
    }

    fun needsUpgrade(value: String): Boolean = !value.startsWith(PREFIX)

    private fun legacyHash(pin: String, salt: String): String = MessageDigest.getInstance("SHA-256")
        .digest("$salt:$pin".toByteArray(StandardCharsets.UTF_8))
        .let(encoder::encodeToString)
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

sealed interface PinChangeResult {
    data class Success(val session: AuthSession) : PinChangeResult
    data class Failure(val message: String) : PinChangeResult
}

interface AuthRepository {
    fun restoredSession(): AuthSession?
    fun login(role: UserRole, phone: String, pin: String): AuthResult
    fun changePin(session: AuthSession, currentPin: String, newPin: String): PinChangeResult
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
        val durable = durablePreferences.getString(KEY_DURABLE_SESSION, null)?.let(SessionCodec::decode)
        if (durable != null) return restoreIfAllowed(durable) { sessionFileStore.write(it) }
        val fileSession = sessionFileStore.read()
        if (fileSession != null) return restoreIfAllowed(fileSession) {
            durablePreferences.edit().putString(KEY_DURABLE_SESSION, SessionCodec.encode(it)).commit()
        }
        val role = preferences.getString(KEY_ROLE, null)?.let { runCatching { UserRole.valueOf(it) }.getOrNull() } ?: return null
        val userId = preferences.getString(KEY_USER_ID, null) ?: return null
        val displayName = preferences.getString(KEY_DISPLAY_NAME, null) ?: return null
        val phone = preferences.getString(KEY_PHONE, null) ?: return null
        return restoreIfAllowed(AuthSession(role, userId, displayName, phone)) { saveSession(it) }
    }

    private fun restoreIfAllowed(session: AuthSession, migrate: (AuthSession) -> Unit): AuthSession? {
        if (!isSessionAllowed(session)) {
            logout()
            return null
        }
        val refreshed = session.copy(mustChangePin = pinChangeRequired(session))
        migrate(refreshed)
        return refreshed
    }

    private fun pinChangeRequired(session: AuthSession): Boolean = when (session.role) {
        UserRole.DOCTOR -> doctorCredential()?.mustChangePin ?: false
        UserRole.ASSISTANT -> credentialRecords().firstOrNull { it.assistantId == session.userId }?.mustChangePin ?: false
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
            UserRole.DOCTOR -> authenticateDoctor(normalized, pin)
            UserRole.ASSISTANT -> authenticateAssistant(normalized, pin)
        } ?: return AuthResult.Failure("Credentials do not match this role, or this account is disabled.")
        if (session.role == UserRole.ASSISTANT && session.userId in removedAssistantIds()) {
            return AuthResult.Failure("This assistant account has been removed by the doctor.")
        }
        return if (saveSession(session)) AuthResult.Success(session) else AuthResult.Failure("Unable to save this session. Please try again.")
    }

    private fun authenticateDoctor(phone: String, pin: String): AuthSession? {
        val stored = doctorCredential()
        if (stored != null) {
            if (stored.phone != phone || !PinHasher.matches(pin, stored.salt, stored.pinHash)) return null
            val effective = upgradeCredential(stored, pin) { upgraded ->
                preferences.edit().putString(KEY_DOCTOR_CREDENTIAL, AssistantCredentialCodec.encode(upgraded)).commit()
            }
            return AuthSession(UserRole.DOCTOR, effective.assistantId, effective.displayName, effective.phone, effective.mustChangePin)
        }
        return DemoCredentials.authenticate(UserRole.DOCTOR, phone, pin)
    }

    private fun authenticateAssistant(phone: String, pin: String): AuthSession? {
        val stored = credentialRecords().firstOrNull { it.phone == phone }
        if (stored != null) {
            if (!stored.active || !PinHasher.matches(pin, stored.salt, stored.pinHash)) return null
            val effective = upgradeCredential(stored, pin, ::saveCredential)
            return AuthSession(UserRole.ASSISTANT, effective.assistantId, effective.displayName, effective.phone, effective.mustChangePin)
        }
        return DemoCredentials.authenticate(UserRole.ASSISTANT, phone, pin)
    }

    private fun upgradeCredential(
        record: AssistantCredentialRecord,
        pin: String,
        save: (AssistantCredentialRecord) -> Boolean
    ): AssistantCredentialRecord {
        if (!PinHasher.needsUpgrade(record.pinHash)) return record
        val salt = PinHasher.newSalt()
        val upgraded = record.copy(salt = salt, pinHash = PinHasher.hash(pin, salt))
        return if (save(upgraded)) upgraded else record
    }
    override fun changePin(session: AuthSession, currentPin: String, newPin: String): PinChangeResult {
        if (!CredentialValidator.isValidPin(currentPin)) return PinChangeResult.Failure("Enter your current 4-digit PIN.")
        if (!CredentialValidator.isAcceptableNewPin(newPin)) return PinChangeResult.Failure("Choose a less predictable 4-digit PIN.")
        if (currentPin == newPin) return PinChangeResult.Failure("The new PIN must be different from the current PIN.")
        val authenticated = when (session.role) {
            UserRole.DOCTOR -> authenticateDoctor(session.phone, currentPin)
            UserRole.ASSISTANT -> authenticateAssistant(session.phone, currentPin)
        }
        if (authenticated?.userId != session.userId) return PinChangeResult.Failure("The current PIN is incorrect.")
        val salt = PinHasher.newSalt()
        val record = AssistantCredentialRecord(
            assistantId = session.userId,
            displayName = session.displayName,
            phone = session.phone,
            active = true,
            salt = salt,
            pinHash = PinHasher.hash(newPin, salt),
            mustChangePin = false
        )
        val credentialSaved = when (session.role) {
            UserRole.DOCTOR -> preferences.edit().putString(KEY_DOCTOR_CREDENTIAL, AssistantCredentialCodec.encode(record)).commit()
            UserRole.ASSISTANT -> saveCredential(record)
        }
        if (!credentialSaved) return PinChangeResult.Failure("Unable to save the new PIN. Please try again.")
        val updated = session.copy(mustChangePin = false)
        return if (saveSession(updated)) PinChangeResult.Success(updated)
        else PinChangeResult.Failure("PIN changed, but the session could not be saved. Please login again.")
    }

    private fun saveSession(session: AuthSession): Boolean {
        val legacy = preferences.edit()
            .putString(KEY_ROLE, session.role.name)
            .putString(KEY_USER_ID, session.userId)
            .putString(KEY_DISPLAY_NAME, session.displayName)
            .putString(KEY_PHONE, session.phone)
            .commit()
        val file = sessionFileStore.write(session)
        val durable = durablePreferences.edit().putString(KEY_DURABLE_SESSION, SessionCodec.encode(session)).commit()
        return durable || legacy || file
    }

    override fun logout() {
        preferences.edit().remove(KEY_ROLE).remove(KEY_USER_ID).remove(KEY_DISPLAY_NAME).remove(KEY_PHONE).commit()
        sessionFileStore.clear()
        durablePreferences.edit().remove(KEY_DURABLE_SESSION).commit()
    }

    override fun removedAssistantIds(): Set<String> = preferences.getStringSet(KEY_REMOVED_ASSISTANTS, emptySet()).orEmpty().toSet()

    override fun provisionAssistant(assistant: Assistant, temporaryPin: String): Boolean =
        saveAssistantCredential(assistant, temporaryPin, mustChangePin = true)

    private fun saveAssistantCredential(assistant: Assistant, pin: String, mustChangePin: Boolean): Boolean {
        if (!CredentialValidator.isValidPin(pin) || !CredentialValidator.isValidPhone(assistant.phone)) return false
        val salt = PinHasher.newSalt()
        return saveCredential(AssistantCredentialRecord(
            assistant.id, assistant.name, CredentialValidator.normalizePhone(assistant.phone), assistant.active,
            salt, PinHasher.hash(pin, salt), mustChangePin
        ))
    }

    override fun setAssistantActive(assistant: Assistant): Boolean {
        val existing = credentialRecords().firstOrNull { it.assistantId == assistant.id }
        if (existing == null) return saveAssistantCredential(assistant, LEGACY_ASSISTANT_PIN, mustChangePin = false)
        return saveCredential(existing.copy(displayName = assistant.name, phone = CredentialValidator.normalizePhone(assistant.phone), active = assistant.active))
    }

    override fun resetAssistantPin(assistant: Assistant, temporaryPin: String): Boolean =
        saveAssistantCredential(assistant, temporaryPin, mustChangePin = true)

    override fun removeAssistant(assistantId: String): Boolean {
        val remaining = credentialRecords().filterNot { it.assistantId == assistantId }
            .mapTo(mutableSetOf(), AssistantCredentialCodec::encode)
        return preferences.edit()
            .putStringSet(KEY_REMOVED_ASSISTANTS, removedAssistantIds() + assistantId)
            .putStringSet(KEY_ASSISTANT_CREDENTIALS, remaining)
            .commit()
    }

    private fun doctorCredential(): AssistantCredentialRecord? =
        preferences.getString(KEY_DOCTOR_CREDENTIAL, null)?.let(AssistantCredentialCodec::decode)

    private fun credentialRecords(): List<AssistantCredentialRecord> =
        preferences.getStringSet(KEY_ASSISTANT_CREDENTIALS, emptySet()).orEmpty().mapNotNull(AssistantCredentialCodec::decode)

    private fun saveCredential(record: AssistantCredentialRecord): Boolean {
        val records = credentialRecords().filterNot { it.assistantId == record.assistantId || it.phone == record.phone } + record
        return preferences.edit().putStringSet(KEY_ASSISTANT_CREDENTIALS, records.mapTo(mutableSetOf(), AssistantCredentialCodec::encode)).commit()
    }

    private companion object {
        const val KEY_ROLE = "auth_role"
        const val KEY_USER_ID = "auth_user_id"
        const val KEY_DISPLAY_NAME = "auth_display_name"
        const val KEY_PHONE = "auth_phone"
        const val KEY_REMOVED_ASSISTANTS = "removed_assistant_ids"
        const val KEY_ASSISTANT_CREDENTIALS = "assistant_credentials_v1"
        const val KEY_DOCTOR_CREDENTIAL = "doctor_credential_v1"
        const val KEY_DURABLE_SESSION = "authenticated_session_v1"
        const val LEGACY_ASSISTANT_PIN = "1234"
    }
}