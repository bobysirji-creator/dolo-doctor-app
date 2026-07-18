package com.dolo.doctor.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.dolo.doctor.data.BackupExportResult
import com.dolo.doctor.data.EncryptedBackupService
import com.dolo.doctor.ui.components.ElevatedSection
import com.dolo.doctor.ui.components.PageHeader
import com.dolo.doctor.ui.components.PrimaryAction
import java.io.ByteArrayOutputStream
import java.time.LocalDate

@Composable
fun BackupScreen(
    onBack: () -> Unit,
    onExport: (String) -> BackupExportResult,
    onRestore: (ByteArray, String) -> String?
) {
    val context = LocalContext.current
    var exportPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var restorePassword by remember { mutableStateOf("") }
    var pendingExport by remember { mutableStateOf<ByteArray?>(null) }
    var pendingRestore by remember { mutableStateOf<ByteArray?>(null) }
    var confirmRestore by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }

    val createDocument = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        val bytes = pendingExport
        pendingExport = null
        if (uri != null && bytes != null) {
            val saved = runCatching {
                context.contentResolver.openOutputStream(uri)?.use { it.write(bytes) } ?: error("Unable to open destination")
            }.isSuccess
            message = if (saved) "Encrypted backup saved. Keep the file and password in separate safe places." else "Backup file could not be saved."
            isError = !saved
            if (saved) { exportPassword = ""; confirmPassword = "" }
        }
    }
    val openDocument = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val bytes = runCatching { readBackup(context, uri) }.getOrNull()
            if (bytes == null) {
                message = "The selected file is invalid or larger than 10 MB."
                isError = true
            } else {
                pendingRestore = bytes
                confirmRestore = true
            }
        }
    }

    Column(
        Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        PageHeader("Backup & recovery", onBack)
        ElevatedSection("Protected clinic backup", "Portable across app reinstalls and phones") {
            Icon(Icons.Outlined.Security, null, tint = MaterialTheme.colorScheme.primary)
            Text("Uses password-based AES-GCM encryption. Your password is never stored in the app.")
            Text("Includes clinic settings, appointments, queues, history, reports, announcements and availability.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Excludes login PINs, assistant credentials, signing keys and provider secrets.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        ElevatedSection("Create encrypted backup") {
            OutlinedTextField(exportPassword, { exportPassword = it }, Modifier.fillMaxWidth(), label = { Text("Backup password") }, singleLine = true, visualTransformation = PasswordVisualTransformation())
            OutlinedTextField(confirmPassword, { confirmPassword = it }, Modifier.fillMaxWidth(), label = { Text("Confirm password") }, singleLine = true, visualTransformation = PasswordVisualTransformation())
            PrimaryAction(
                "Choose save location",
                {
                    val result = onExport(exportPassword)
                    val bytes = result.bytes
                    if (bytes == null) {
                        message = result.error ?: "Unable to create backup."
                        isError = true
                    } else {
                        pendingExport = bytes
                        createDocument.launch("dolo-doctor-backup-${LocalDate.now()}.dolo")
                    }
                },
                enabled = exportPassword.length >= 8 && exportPassword == confirmPassword,
                icon = Icons.Outlined.Backup
            )
            if (exportPassword.isNotEmpty() && exportPassword.length < 8) Text("Use at least 8 characters.", color = MaterialTheme.colorScheme.error)
            else if (confirmPassword.isNotEmpty() && exportPassword != confirmPassword) Text("Passwords do not match.", color = MaterialTheme.colorScheme.error)
        }
        ElevatedSection("Restore encrypted backup", "This replaces local clinic workflow data after confirmation") {
            OutlinedTextField(restorePassword, { restorePassword = it }, Modifier.fillMaxWidth(), label = { Text("Backup password") }, singleLine = true, visualTransformation = PasswordVisualTransformation())
            PrimaryAction(
                "Select backup file",
                { openDocument.launch(arrayOf("application/octet-stream", "application/*", "text/plain")) },
                enabled = restorePassword.length >= 8,
                icon = Icons.Outlined.Restore
            )
            Text("The current signed-in account and assistant credentials on this device are retained.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        message?.let { Text(it, color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary) }
    }

    if (confirmRestore) {
        AlertDialog(
            onDismissRequest = { confirmRestore = false; pendingRestore = null },
            title = { Text("Replace local clinic data?") },
            text = { Text("Current appointments, queues, history and clinic settings will be replaced by this backup. Login credentials will not change.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmRestore = false
                    val bytes = pendingRestore
                    pendingRestore = null
                    if (bytes != null) {
                        val error = onRestore(bytes, restorePassword)
                        message = error ?: "Backup restored successfully. Review today's date and queue before continuing."
                        isError = error != null
                        if (error == null) restorePassword = ""
                    }
                }) { Text("Restore") }
            },
            dismissButton = { TextButton(onClick = { confirmRestore = false; pendingRestore = null }) { Text("Cancel") } }
        )
    }
}

private fun readBackup(context: Context, uri: Uri): ByteArray {
    val input = context.contentResolver.openInputStream(uri) ?: error("Unable to open file")
    return input.use {
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(8192)
        var total = 0
        while (true) {
            val count = it.read(buffer)
            if (count < 0) break
            total += count
            require(total <= EncryptedBackupService.MAX_FILE_SIZE)
            output.write(buffer, 0, count)
        }
        output.toByteArray()
    }
}
