package com.dolo.doctor.auth

import android.util.AtomicFile
import com.dolo.doctor.data.model.UserRole
import java.io.File
import java.nio.charset.StandardCharsets

object SessionCodec {
    private const val SEPARATOR = "\t"

    fun encode(session: AuthSession): String = listOf(
        session.role.name,
        session.userId,
        session.displayName,
        session.phone
    ).joinToString(SEPARATOR)

    fun decode(value: String): AuthSession? {
        val parts = value.trim().split(SEPARATOR)
        if (parts.size != 4) return null
        val role = runCatching { UserRole.valueOf(parts[0]) }.getOrNull() ?: return null
        if (parts[1].isBlank() || parts[2].isBlank() || !CredentialValidator.isValidPhone(parts[3])) return null
        return AuthSession(role, parts[1], parts[2], CredentialValidator.normalizePhone(parts[3]))
    }
}

class SessionFileStore(file: File) {
    private val file = AtomicFile(file)

    fun read(): AuthSession? = runCatching {
        SessionCodec.decode(String(file.readFully(), StandardCharsets.UTF_8))
    }.getOrNull()

    fun write(session: AuthSession): Boolean {
        var output: java.io.FileOutputStream? = null
        return try {
            val stream = file.startWrite()
            output = stream
            stream.write(SessionCodec.encode(session).toByteArray(StandardCharsets.UTF_8))
            stream.flush()
            file.finishWrite(stream)
            true
        } catch (_: Exception) {
            output?.let { file.failWrite(it) }
            false
        }
    }

    fun clear() = file.delete()
}