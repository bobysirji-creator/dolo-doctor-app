package com.dolo.doctor.data

import com.dolo.doctor.data.model.DoctorUiState
import com.dolo.doctor.data.model.SyncStatus
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import java.time.LocalDate
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

data class BackupExportResult(val bytes: ByteArray? = null, val error: String? = null)
data class BackupRestoreResult(val state: DoctorUiState? = null, val error: String? = null)

class DoctorBackupManager(private val crypto: EncryptedBackupService = EncryptedBackupService()) {
    fun export(state: DoctorUiState, password: CharArray): BackupExportResult = try {
        BackupExportResult(bytes = crypto.encrypt(DoctorBackupCodec.encode(state).toByteArray(StandardCharsets.UTF_8), password))
    } catch (error: Exception) { BackupExportResult(error = error.message ?: "Unable to create backup.") }
    finally { password.fill('\u0000') }

    fun restore(bytes: ByteArray, password: CharArray, currentState: DoctorUiState): BackupRestoreResult = try {
        BackupRestoreResult(state = DoctorBackupCodec.decode(String(crypto.decrypt(bytes, password), StandardCharsets.UTF_8), currentState))
    } catch (_: Exception) { BackupRestoreResult(error = "Backup could not be opened. Check the password and selected file.") }
    finally { password.fill('\u0000') }
}

class EncryptedBackupService(private val random: SecureRandom = SecureRandom(), private val iterations: Int = 150_000) {
    fun encrypt(plainText: ByteArray, password: CharArray): ByteArray {
        require(password.size >= 8) { "Use a password of at least 8 characters." }
        val salt = ByteArray(16).also(random::nextBytes)
        val iv = ByteArray(12).also(random::nextBytes)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key(password, salt), GCMParameterSpec(128, iv))
        cipher.updateAAD(MAGIC)
        val encrypted = cipher.doFinal(plainText)
        return ByteBuffer.allocate(MAGIC.size + 28 + encrypted.size).put(MAGIC).put(salt).put(iv).put(encrypted).array()
    }
    fun decrypt(payload: ByteArray, password: CharArray): ByteArray {
        require(password.size >= 8 && payload.size in 51..MAX_FILE_SIZE)
        val buffer = ByteBuffer.wrap(payload)
        val magic = ByteArray(MAGIC.size).also(buffer::get)
        require(magic.contentEquals(MAGIC))
        val salt = ByteArray(16).also(buffer::get)
        val iv = ByteArray(12).also(buffer::get)
        val encrypted = ByteArray(buffer.remaining()).also(buffer::get)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key(password, salt), GCMParameterSpec(128, iv))
        cipher.updateAAD(MAGIC)
        return cipher.doFinal(encrypted)
    }
    private fun key(password: CharArray, salt: ByteArray): SecretKeySpec {
        val spec = PBEKeySpec(password, salt, iterations, 256)
        return try { SecretKeySpec(SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded, "AES") }
        finally { spec.clearPassword() }
    }
    companion object {
        private val MAGIC = "DOLO13\n".toByteArray(StandardCharsets.US_ASCII)
        const val MAX_FILE_SIZE = 10 * 1024 * 1024
    }
}

internal object DoctorBackupCodec {
    private const val HEADER = "DOLO_DOCTOR_BACKUP_V1"
    private val encoder = Base64.getUrlEncoder().withoutPadding()
    private val decoder = Base64.getUrlDecoder()

    fun encode(state: DoctorUiState): String = buildList {
        add(HEADER)
        add(record("DATE", state.queueDate)); add(record("SESSION", state.selectedSession))
        add(record("PROFILE", QueueStateCodec.encodeProfile(state.profile)))
        state.clinics.forEach { add(record("CLINIC", QueueStateCodec.encodeClinic(it))) }
        state.sessionQueues.forEach { add(record("QUEUE", QueueStateCodec.encodeSessionQueue(it))) }
        state.appointments.forEach { add(record("APPOINTMENT", QueueStateCodec.encodeAppointment(it))) }
        state.queueHistory.forEach { add(record("HISTORY", QueueStateCodec.encodeHistory(it))) }
        state.auditEvents.forEach { add(record("AUDIT", QueueStateCodec.encodeAuditEvent(it))) }
        state.announcements.forEach { add(record("ANNOUNCEMENT", QueueStateCodec.encodeAnnouncement(it))) }
        state.availabilityBlocks.forEach { add(record("AVAILABILITY", QueueStateCodec.encodeAvailabilityBlock(it))) }
        state.feedback.forEach { add(record("FEEDBACK", QueueStateCodec.encodeFeedback(it))) }
        state.queueDelayNotices.forEach { add(record("DELAY", QueueStateCodec.encodeQueueDelayNotice(it))) }
    }.joinToString("\n")

    fun decode(value: String, current: DoctorUiState): DoctorUiState {
        require(value.length <= EncryptedBackupService.MAX_FILE_SIZE)
        val lines = value.lineSequence().filter(String::isNotBlank).toList()
        require(lines.size in 4..10_000 && lines.first() == HEADER)
        val records = lines.drop(1).groupBy({ it.substringBefore('|') }, { decodeRecord(it) })
        require(records.keys.all { it in ALLOWED })
        val date = records.one("DATE"); LocalDate.parse(date)
        val session = records.one("SESSION").also { require(it in setOf("Morning", "Evening")) }
        val profile = QueueStateCodec.decodeProfile(records.one("PROFILE")) ?: error("Invalid profile")
        val clinics = records.list("CLINIC", QueueStateCodec::decodeClinic).also { require(it.isNotEmpty()) }
        val queues = records.list("QUEUE", QueueStateCodec::decodeSessionQueue)
        require(queues.size == 2 && queues.map { it.session }.toSet() == setOf("Morning", "Evening"))
        val morning = queues.single { it.session == "Morning" }
        return current.copy(
            profile = profile, clinics = clinics, appointments = records.list("APPOINTMENT", QueueStateCodec::decodeAppointment),
            announcements = records.list("ANNOUNCEMENT", QueueStateCodec::decodeAnnouncement),
            availabilityBlocks = records.list("AVAILABILITY", QueueStateCodec::decodeAvailabilityBlock),
            queueDate = date, queueHistory = records.list("HISTORY", QueueStateCodec::decodeHistory),
            auditEvents = records.list("AUDIT", QueueStateCodec::decodeAuditEvent).takeLast(500),
            sessionQueues = queues, queueState = morning.state, currentToken = morning.currentToken,
            selectedSession = session, notificationReadThrough = 0,
            feedback = records.list("FEEDBACK", QueueStateCodec::decodeFeedback),
            queueDelayNotices = records.list("DELAY", QueueStateCodec::decodeQueueDelayNotice),
            syncRevision = 0, syncStatus = SyncStatus.LOCAL_ONLY, lastSyncedAt = "",
            syncMessage = "Restored encrypted local backup; publish when shared sync is available."
        )
    }
    private fun record(type: String, value: String) = "$type|" + encoder.encodeToString(value.toByteArray(StandardCharsets.UTF_8))
    private fun decodeRecord(line: String): String {
        require('|' in line)
        return String(decoder.decode(line.substringAfter('|')), StandardCharsets.UTF_8)
    }
    private fun Map<String, List<String>>.one(type: String) = get(type)?.singleOrNull() ?: error("Missing or repeated $type")
    private fun <T> Map<String, List<String>>.list(type: String, decode: (String) -> T?) =
        get(type).orEmpty().map { decode(it) ?: error("Invalid $type") }
    private val ALLOWED = setOf("DATE","SESSION","PROFILE","CLINIC","QUEUE","APPOINTMENT","HISTORY","AUDIT","ANNOUNCEMENT","AVAILABILITY","FEEDBACK","DELAY")
}
