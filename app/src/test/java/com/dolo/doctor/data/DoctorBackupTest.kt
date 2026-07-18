package com.dolo.doctor.data

import com.dolo.doctor.data.model.SyncStatus
import com.dolo.doctor.data.model.UserRole
import org.junit.Assert.*
import org.junit.Test

class DoctorBackupTest {
    private val password = "Strong backup 2026"

    @Test fun encryptedBackupRoundTripRestoresOperationalData() {
        val source = DummyData.initialState("2026-07-18").copy(
            role = UserRole.DOCTOR,
            selectedSession = "Evening",
            syncRevision = 42,
            syncStatus = SyncStatus.SYNCED
        )
        val current = DummyData.initialState("2026-07-19").copy(
            role = UserRole.DOCTOR,
            assistants = source.assistants.take(1)
        )
        val manager = DoctorBackupManager()

        val exported = manager.export(source, password.toCharArray())
        val restored = manager.restore(requireNotNull(exported.bytes), password.toCharArray(), current).state
            ?: throw AssertionError("Backup did not restore")

        assertEquals(source.profile, restored.profile)
        assertEquals(source.clinics, restored.clinics)
        assertEquals(source.appointments, restored.appointments)
        assertEquals(source.sessionQueues, restored.sessionQueues)
        assertEquals("Evening", restored.selectedSession)
        assertEquals(current.assistants, restored.assistants)
        assertEquals(UserRole.DOCTOR, restored.role)
        assertEquals(0, restored.syncRevision)
        assertEquals(SyncStatus.LOCAL_ONLY, restored.syncStatus)
    }

    @Test fun wrongPasswordCannotOpenBackup() {
        val manager = DoctorBackupManager()
        val exported = manager.export(DummyData.initialState("2026-07-18"), password.toCharArray())

        val result = manager.restore(requireNotNull(exported.bytes), "incorrect password".toCharArray(), DummyData.initialState("2026-07-18"))

        assertNull(result.state)
        assertNotNull(result.error)
    }

    @Test fun tamperedBackupIsRejectedByAuthenticationTag() {
        val manager = DoctorBackupManager()
        val bytes = requireNotNull(manager.export(DummyData.initialState("2026-07-18"), password.toCharArray()).bytes)
        bytes[bytes.lastIndex] = (bytes.last().toInt() xor 1).toByte()

        assertNull(manager.restore(bytes, password.toCharArray(), DummyData.initialState("2026-07-18")).state)
    }

    @Test fun shortPasswordAndUnencryptedFileAreRejected() {
        val manager = DoctorBackupManager()
        assertNull(manager.export(DummyData.initialState("2026-07-18"), "short".toCharArray()).bytes)
        assertNull(manager.restore("plain text".toByteArray(), password.toCharArray(), DummyData.initialState("2026-07-18")).state)
    }

    @Test fun malformedPayloadCannotReplaceCurrentState() {
        val current = DummyData.initialState("2026-07-18")
        val crypto = EncryptedBackupService()
        val encrypted = crypto.encrypt("DOLO_DOCTOR_BACKUP_V1\nDATE|bad".toByteArray(), password.toCharArray())

        assertNull(DoctorBackupManager(crypto).restore(encrypted, password.toCharArray(), current).state)
    }

    @Test fun assistantsCannotExportOrRestoreDoctorBackup() {
        val model = DoctorViewModel()
        model.login(UserRole.ASSISTANT, "staff-1")

        assertNull(model.exportEncryptedBackup(password).bytes)
        assertNotNull(model.restoreEncryptedBackup(byteArrayOf(1, 2, 3), password))
    }
}
