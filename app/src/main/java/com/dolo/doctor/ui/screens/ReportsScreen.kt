package com.dolo.doctor.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dolo.doctor.data.model.*
import com.dolo.doctor.ui.components.ElevatedSection
import com.dolo.doctor.ui.components.MetricTile
import com.dolo.doctor.ui.components.PageHeader
import com.dolo.doctor.ui.components.PrimaryAction
import com.dolo.doctor.ui.components.StatusPill
import kotlin.math.roundToInt

@Composable fun ReportsScreen(
    state: DoctorUiState,
    permissions: Set<Permission>,
    report: OperationalReport,
    onBack: () -> Unit,
    onAcknowledgeFeedback: (String) -> Boolean,
    onSendDelayNotice: (String, Int, String) -> String?
) {
    val doctorMode = state.role == UserRole.DOCTOR
    val canViewFeedback = doctorMode || Permission.VIEW_PATIENT_FEEDBACK in permissions
    val canSendDelay = doctorMode || Permission.SEND_QUEUE_DELAY_NOTICE in permissions
    var showDelayDialog by remember { mutableStateOf(false) }
    val displayedRating = (report.averageRating * 10).roundToInt() / 10.0

    LazyColumn(
        Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        item { PageHeader("Reports & feedback", onBack) }
        item {
            ElevatedSection("Operational summary", "Current and archived local appointments") {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricTile("Appointments", report.appointments.toString(), Modifier.weight(1f), MaterialTheme.colorScheme.primary)
                    MetricTile("Completed", report.completed.toString(), Modifier.weight(1f), MaterialTheme.colorScheme.tertiary)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricTile("Absent", report.absent.toString(), Modifier.weight(1f), MaterialTheme.colorScheme.error)
                    MetricTile("Fee pending", report.pending.toString(), Modifier.weight(1f), MaterialTheme.colorScheme.secondary)
                }
                Text("Confirmed collections: INR ${report.collectedFees}", fontWeight = FontWeight.Bold)
            }
        }
        item {
            ElevatedSection("Patient feedback summary") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Insights, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(10.dp))
                    Text("$displayedRating / 5 from ${report.feedbackCount} response(s)", fontWeight = FontWeight.Bold)
                }
                if (!canViewFeedback) {
                    Text("Detailed feedback requires the View patient feedback permission.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        if (canViewFeedback) {
            items(state.feedback.sortedByDescending { it.submittedOn }, key = { it.id }) { feedback ->
                ElevatedSection("${feedback.rating} / 5 • ${feedback.patientName}", feedback.submittedOn) {
                    Text(feedback.comment, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        StatusPill(if (feedback.acknowledged) "Acknowledged" else "New feedback", feedback.acknowledged)
                        Spacer(Modifier.weight(1f))
                        if (!feedback.acknowledged) {
                            TextButton(onClick = { onAcknowledgeFeedback(feedback.id) }) {
                                Icon(Icons.Outlined.CheckCircle, null)
                                Spacer(Modifier.width(5.dp))
                                Text("Acknowledge")
                            }
                        }
                    }
                }
            }
        }
        item {
            ElevatedSection("Queue-delay notices", "Local Patient App delivery contract") {
                Text(
                    "Record a session delay notice now. Real patient delivery begins when the shared backend connects both apps.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                PrimaryAction("Create delay notice", { showDelayDialog = true }, enabled = canSendDelay, icon = Icons.Outlined.Campaign)
            }
        }
        items(state.queueDelayNotices.sortedWith(compareByDescending<QueueDelayNotice> { it.createdOn }.thenByDescending { it.createdAt }), key = { it.id }) { notice ->
            ElevatedSection("${notice.session} • ${notice.delayMinutes} minute delay", "${notice.createdOn} at ${notice.createdAt}") {
                Text(notice.message, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Created by ${notice.createdBy}", style = MaterialTheme.typography.bodySmall)
            }
        }
        item {
            ElevatedSection("Multi-clinic readiness", "${report.clinicCount} configured clinic record(s)") {
                state.clinics.forEach { clinic ->
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Outlined.LocationOn, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(9.dp))
                        Column {
                            Text(clinic.name, fontWeight = FontWeight.Bold)
                            Text(clinic.address, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                Text(
                    "Clinic records and service contracts are ID-scoped. Independent multi-clinic queues and cross-device selection become server-authoritative in Stage 10.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (showDelayDialog) {
        QueueDelayDialog(
            onDismiss = { showDelayDialog = false },
            onSend = onSendDelayNotice
        )
    }
}

@Composable private fun QueueDelayDialog(
    onDismiss: () -> Unit,
    onSend: (String, Int, String) -> String?
) {
    var session by remember { mutableStateOf("Morning") }
    var minutes by remember { mutableStateOf("15") }
    var message by remember { mutableStateOf("The queue is running later than expected. Please follow the updated wait estimate.") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create queue-delay notice") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Morning", "Evening").forEach { option ->
                        FilterChip(
                            selected = session == option,
                            onClick = { session = option; error = null },
                            label = { Text(option) }
                        )
                    }
                }
                OutlinedTextField(
                    value = minutes,
                    onValueChange = { minutes = it.filter(Char::isDigit).take(3); error = null },
                    label = { Text("Delay minutes") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it.take(160); error = null },
                    label = { Text("Patient message") },
                    minLines = 3,
                    supportingText = { Text("${message.length}/160") }
                )
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            Button(onClick = {
                val result = onSend(session, minutes.toIntOrNull() ?: -1, message)
                error = result
                if (result == null) onDismiss()
            }) { Text("Save notice") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
