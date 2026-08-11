package com.dolo.doctor.authority

import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import org.json.JSONObject
import java.security.KeyStore
import java.security.MessageDigest
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

enum class AuthoritySource { NOT_CHECKED, LIVE, CACHED_FRESH, CACHED_STALE }

data class AuthorityFreshness(
    val source: AuthoritySource = AuthoritySource.NOT_CHECKED,
    val savedAtEpochMs: Long? = null
) {
    fun message(): String = when (source) {
        AuthoritySource.NOT_CHECKED -> "Hosted authority has not been checked."
        AuthoritySource.LIVE -> "Live server data is authoritative."
        AuthoritySource.CACHED_FRESH -> "Showing encrypted cached server data. Refresh when online."
        AuthoritySource.CACHED_STALE -> "Showing older encrypted server data. Confirm online before any action."
    }
}

data class AuthorityCacheEntry(val body: String, val freshness: AuthorityFreshness)

object AuthorityRehearsalPolicy {
    const val FRESH_CACHE_MS = 15 * 60 * 1000L
    const val MAX_CACHE_MS = 24 * 60 * 60 * 1000L
    const val MAX_COMMAND_ATTEMPTS = 2

    fun classifyAge(ageMs: Long): AuthoritySource = when {
        ageMs < 0L -> AuthoritySource.CACHED_STALE
        ageMs <= FRESH_CACHE_MS -> AuthoritySource.CACHED_FRESH
        else -> AuthoritySource.CACHED_STALE
    }

    fun canRetry(method: String, hasIdempotencyKey: Boolean, completedAttempts: Int, status: Int? = null): Boolean =
        method.uppercase() != "GET" &&
            hasIdempotencyKey &&
            completedAttempts < MAX_COMMAND_ATTEMPTS &&
            (status == null || status in setOf(502, 503, 504))

    fun canUseCacheAfterFailure(status: Int?): Boolean = status == null || status in setOf(502, 503, 504)

    fun conflictMessage(status: Int): String? =
        if (status == 409) "Server state changed. Refresh authoritative data before trying again." else null
}

class AuthorityTracker {
    @Volatile private var current = AuthorityFreshness()

    @Synchronized fun beginOperation() { current = AuthorityFreshness() }

    @Synchronized fun recordLive() {
        if (current.source == AuthoritySource.NOT_CHECKED) current = AuthorityFreshness(AuthoritySource.LIVE)
    }

    @Synchronized fun recordCache(freshness: AuthorityFreshness) { current = freshness }

    fun snapshot(): AuthorityFreshness = current
}

class EncryptedAuthorityReadCache(
    private val preferences: SharedPreferences,
    private val keyAlias: String = "dolo_doctor_authority_cache_v1"
) {
    fun put(requestKey: String, body: String, nowEpochMs: Long = System.currentTimeMillis()) {
        val payload = JSONObject().put("savedAt", nowEpochMs).put("body", body).toString()
        val cipher = Cipher.getInstance(TRANSFORM).apply { init(Cipher.ENCRYPT_MODE, key()) }
        val encrypted = cipher.doFinal(payload.toByteArray(Charsets.UTF_8))
        val ivKey = storageKey(requestKey, "iv")
        val dataKey = storageKey(requestKey, "data")
        val tracked = preferences.getStringSet(indexKey(), emptySet()).orEmpty().toMutableSet().apply {
            add(ivKey)
            add(dataKey)
        }
        preferences.edit()
            .putString(ivKey, Base64.getEncoder().encodeToString(cipher.iv))
            .putString(dataKey, Base64.getEncoder().encodeToString(encrypted))
            .putStringSet(indexKey(), tracked)
            .apply()
    }

    fun read(requestKey: String, nowEpochMs: Long = System.currentTimeMillis()): AuthorityCacheEntry? = runCatching {
        val iv = preferences.getString(storageKey(requestKey, "iv"), null) ?: return null
        val encrypted = preferences.getString(storageKey(requestKey, "data"), null) ?: return null
        val cipher = Cipher.getInstance(TRANSFORM).apply {
            init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, Base64.getDecoder().decode(iv)))
        }
        val payload = JSONObject(String(cipher.doFinal(Base64.getDecoder().decode(encrypted)), Charsets.UTF_8))
        val savedAt = payload.getLong("savedAt")
        val age = nowEpochMs - savedAt
        if (age > AuthorityRehearsalPolicy.MAX_CACHE_MS) {
            remove(requestKey)
            return null
        }
        AuthorityCacheEntry(
            payload.getString("body"),
            AuthorityFreshness(AuthorityRehearsalPolicy.classifyAge(age), savedAt)
        )
    }.getOrElse {
        remove(requestKey)
        null
    }

    fun remove(requestKey: String) {
        val ivKey = storageKey(requestKey, "iv")
        val dataKey = storageKey(requestKey, "data")
        val tracked = preferences.getStringSet(indexKey(), emptySet()).orEmpty().toMutableSet().apply {
            remove(ivKey)
            remove(dataKey)
        }
        preferences.edit().remove(ivKey).remove(dataKey).putStringSet(indexKey(), tracked).apply()
    }

    fun clearAll() {
        val tracked = preferences.getStringSet(indexKey(), emptySet()).orEmpty()
        val editor = preferences.edit()
        tracked.forEach { editor.remove(it) }
        editor.remove(indexKey()).apply()
    }

    private fun indexKey(): String = "authority_cache_index_" + keyAlias.hashCode()

    private fun storageKey(requestKey: String, suffix: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(requestKey.toByteArray(Charsets.UTF_8))
        return "authority_cache_" + digest.joinToString("") { "%02x".format(it) } + "_" + suffix
    }

    private fun key(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (store.getKey(keyAlias, null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply {
            init(
                KeyGenParameterSpec.Builder(keyAlias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build()
            )
        }.generateKey()
    }

    private companion object { const val TRANSFORM = "AES/GCM/NoPadding" }
}