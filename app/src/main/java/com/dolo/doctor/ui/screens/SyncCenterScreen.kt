package com.dolo.doctor.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dolo.doctor.data.model.BookingSource
import com.dolo.doctor.data.model.DoctorUiState
import com.dolo.doctor.data.model.PaymentStatus
import com.dolo.doctor.data.model.SyncStatus
import com.dolo.doctor.ui.components.ElevatedSection
import com.dolo.doctor.ui.components.PageHeader
import com.dolo.doctor.ui.components.PrimaryAction
import com.dolo.doctor.ui.components.StatusPill

@Composable fun SyncCenterScreen(
    state: DoctorUiState,
    onBack: () -> Unit,
    onPublish: () -> String?,
    onPull: () -> String?,
    onSimulatePatientBooking: (String, String, String, String) -> String?
) {
    var resultMessage by remember { mutableStateOf<String?>(null) }
    var showPatientBooking by remember { mutableStateOf(false) }
    val patientBookings = state.appointments.filter { it.bookingSource == BookingSource.PATIENT_APP }

    LazyColumn(
        Modifier.fillMaxSize().safeDrawingPadding().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        item { PageHeader("Shared sync center", onBack) }
        item {
            ElevatedSection("Stage 10 local transport", "Safe contract testing without a live server") {
                Text(
                    "This mock transport runs only inside this Doctor App process. It does not communicate with another phone or the Patient App repository.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                StatusPill(
                    state.syncStatus.name.replace("_", " "),
                    state.syncStatus == SyncStatus.SYNCED
                )
                Text("Applied shared revision: ${state.syncRevision}", fontWeight = FontWeight.Bold)
                Text(
                    if (state.lastSyncedAt.isBlank()) "Never synchronized" else "Last synchronized: ${state.lastSyncedAt}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(state.syncMessage, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item {
            ElevatedSection("1. Publish local clinic snapshot") {
                Text(
                    "Creates or updates the mock server snapshot using a revision and an idempotency key. Stale writes are rejected.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                PrimaryAction(
                    "Publish local snapshot",
                    {
                        resultMessage = onPublish() ?: "Local clinic snapshot published successfully."
                    },
                    icon = Icons.Outlined.CloudSync
                )
            }
        }
        item {
            ElevatedSection("2. Patient App booking simulation") {
                Text(
                    "Adds a fee-pending online booking through the shared contract. It receives an independent session token but cannot enter the consultation queue until clinic payment and receipt confirmation.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                PrimaryAction(
                    "Simulate Patient App booking",
                    { showPatientBooking = true },
                    enabled = state.syncRevision > 0 && state.syncStatus == SyncStatus.SYNCED,
                    icon = Icons.Outlined.PersonAdd
                )
            }
        }
        item {
            ElevatedSection("3. Pull latest shared snapshot") {
                Text(
                    "Applies the latest mock server revision to appointments, queues, updates, availability and delay notices.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                PrimaryAction(
                    "Pull latest snapshot",
                    {
                        resultMessage = onPull() ?: "Latest shared snapshot applied."
                    },
                    enabled = state.syncRevision > 0,
                    icon = Icons.Outlined.CloudDownload
                )
            }
        }
        item {
            ElevatedSection("Patient App contract records", "${patientBookings.size} online appointment(s)") {
                patientBookings.sortedWith(compareBy({ it.session }, { it.token })).takeLast(8).forEach { appointment ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f)) {
                            Text("${appointment.session} token ${appointment.token}", fontWeight = FontWeight.Bold)
                            Text(appointment.patientName, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        StatusPill(
                            if (appointment.paymentStatus == PaymentStatus.PENDING) "Clinic fee pending" else appointment.status.name,
                            appointment.paymentStatus != PaymentStatus.PENDING
                        )
                    }
                }
            }
        }
        item {
            ElevatedSection("Production boundary") {
                Text(
                    "A hosted HTTPS backend must replace this transport before real cross-device use. It must own authentication, authorization, clinic time, token transactions, audit retention and push delivery.",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    resultMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { resultMessage = null },
            title = { Text(if (state.syncStatus == SyncStatus.ERROR || state.syncStatus == SyncStatus.CONFLICT) "Sync needs attention" else "Sync center") },
            text = { Text(message) },
            confirmButton = { TextButton(onClick = { resultMessage = null }) { Text("OK") } }
        )
    }

    if (showPatientBooking) {
        PatientBookingSimulationDialog(
            onDismiss = { showPatientBooking = false },
            onCreate = { name, phone, type, session ->
                val error = onSimulatePatientBooking(name, phone, type, session)
                if (error == null) {
                    showPatientBooking = false
                    resultMessage = "Patient App booking received in Today's Appointments."
                }
                error
            }
        )
    }
}

@Composable private fun PatientBookingSimulationDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String, String, String) -> String?
) {
    var name by remember { mutableStateOf("Demo Online Patient") }
    var phone by remember { mutableStateOf("9876501111") }
    var patientType by remember { mutableStateOf("Self") }
    var session by remember { mutableStateOf("Evening") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Simulate Patient App booking") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    name,
                    { name = it.take(60); error = null },
                    label = { Text("Patient name") },
                    singleLine = true
                )
                OutlinedTextField(
                    phone,
                    { phone = it.filter(Char::isDigit).take(10); error = null },
                    label = { Text("10-digit mobile number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true
                )
                Text("Patient", fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Self", "Family member").forEach { option ->
                        FilterChip(
                            selected = patientType == option,
                            onClick = { patientType = option; error = null },
                            label = { Text(option) }
                        )
                    }
                }
                Text("Session", fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Morning", "Evening").forEach { option ->
                        FilterChip(
                            selected = session == option,
                            onClick = { session = option; error = null },
                            label = { Text(option) }
                        )
                    }
                }
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            Button(onClick = { error = onCreate(name, phone, patientType, session) }) {
                Text("Create booking")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}