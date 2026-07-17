package com.dolo.doctor.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dolo.doctor.data.model.*
import com.dolo.doctor.ui.components.*

@Composable
fun AvailabilityManagementScreen(
    state: DoctorUiState,
    onBack: () -> Unit,
    onSave: (AvailabilityBlock) -> String?,
    onSetAppointmentsEnabled: (String, Boolean) -> Boolean,
    onDelete: (String) -> Boolean,
    onUpdateAffectedPatient: (String, AvailabilityImpactStatus) -> Boolean
) {
    var editingBlock by remember { mutableStateOf<AvailabilityBlock?>(null) }
    var deletingBlock by remember { mutableStateOf<AvailabilityBlock?>(null) }
    val clinic = state.clinics.firstOrNull()

    LazyColumn(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).safeDrawingPadding().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        item { PageHeader("Availability", onBack) }
        item {
            ElevatedSection(
                "Booking control",
                "Use this page for exceptional dates or ranges. Configure recurring weekday/session closures under Clinic & schedule."
            ) {
                Text(
                    "Active blocks prevent new Patient App and clinic walk-in bookings. Existing appointments remain visible until contacted or marked for rescheduling.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                PrimaryAction(
                    "Add availability block",
                    {
                        if (clinic != null) {
                            editingBlock = AvailabilityBlock(
                                id = "",
                                clinicId = clinic.id,
                                fromDate = state.queueDate,
                                toDate = state.queueDate,
                                sessions = "Both",
                                reason = "",
                                appointmentsEnabled = false
                            )
                        }
                    },
                    enabled = clinic != null,
                    icon = Icons.Outlined.EventBusy
                )
            }
        }

        if (state.availabilityBlocks.isEmpty()) {
            item {
                ElevatedSection("No availability blocks") {
                    Text("Appointments follow the normal clinic schedule.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        items(state.availabilityBlocks, key = { it.id }) { block ->
            val affected = state.appointments.filter { it.availabilityBlockId == block.id }
            ElevatedSection(
                block.fromDate + " to " + block.toDate,
                block.sessions + " - " + block.reason
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    StatusPill(
                        if (block.appointmentsEnabled) "Bookings enabled" else "Bookings disabled",
                        block.appointmentsEnabled
                    )
                    Spacer(Modifier.weight(1f))
                    Switch(
                        checked = block.appointmentsEnabled,
                        onCheckedChange = { onSetAppointmentsEnabled(block.id, it) }
                    )
                }
                Text(
                    affected.size.toString() + " current appointment(s) affected",
                    color = if (affected.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.SemiBold
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { editingBlock = block }) {
                        Icon(Icons.Outlined.Edit, null)
                        Spacer(Modifier.width(4.dp))
                        Text("Edit")
                    }
                    TextButton(
                        onClick = { deletingBlock = block },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Outlined.DeleteOutline, null)
                        Spacer(Modifier.width(4.dp))
                        Text("Delete")
                    }
                }

                affected.sortedBy { it.token }.forEach { appointment ->
                    Surface(
                        Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        appointment.session + " token " + appointment.token + " - " + appointment.patientName,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        appointment.patientPhone.ifBlank { "No mobile number in local demo" },
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 12.sp
                                    )
                                }
                                StatusPill(
                                    appointment.availabilityImpactStatus.name.replace("_", " "),
                                    appointment.availabilityImpactStatus in setOf(
                                        AvailabilityImpactStatus.PATIENT_NOTIFIED,
                                        AvailabilityImpactStatus.RESOLVED
                                    )
                                )
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                if (appointment.availabilityImpactStatus != AvailabilityImpactStatus.PATIENT_NOTIFIED) {
                                    AssistChip(
                                        onClick = { onUpdateAffectedPatient(appointment.id, AvailabilityImpactStatus.PATIENT_NOTIFIED) },
                                        label = { Text("Mark notified") },
                                        leadingIcon = { Icon(Icons.Outlined.NotificationsActive, null) }
                                    )
                                }
                                if (appointment.availabilityImpactStatus != AvailabilityImpactStatus.RESCHEDULE_REQUIRED) {
                                    AssistChip(
                                        onClick = { onUpdateAffectedPatient(appointment.id, AvailabilityImpactStatus.RESCHEDULE_REQUIRED) },
                                        label = { Text("Needs reschedule") },
                                        leadingIcon = { Icon(Icons.Outlined.EventRepeat, null) }
                                    )
                                }
                                if (appointment.availabilityImpactStatus != AvailabilityImpactStatus.RESOLVED) {
                                    AssistChip(
                                        onClick = { onUpdateAffectedPatient(appointment.id, AvailabilityImpactStatus.RESOLVED) },
                                        label = { Text("Resolved") },
                                        leadingIcon = { Icon(Icons.Outlined.CheckCircle, null) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    editingBlock?.let { block ->
        AvailabilityEditDialog(
            block = block,
            onDismiss = { editingBlock = null },
            onSave = {
                val result = onSave(it)
                if (result == null) editingBlock = null
                result
            }
        )
    }

    deletingBlock?.let { block ->
        AlertDialog(
            onDismissRequest = { deletingBlock = null },
            icon = { Icon(Icons.Outlined.DeleteOutline, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Delete availability block?") },
            text = { Text("Bookings will reopen unless another active block covers the same date and session. Affected-patient flags will be recalculated.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (onDelete(block.id)) deletingBlock = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { deletingBlock = null }) { Text("Keep block") } }
        )
    }
}

@Composable
private fun AvailabilityEditDialog(
    block: AvailabilityBlock,
    onDismiss: () -> Unit,
    onSave: (AvailabilityBlock) -> String?
) {
    var fromDate by remember(block.id) { mutableStateOf(block.fromDate) }
    var toDate by remember(block.id) { mutableStateOf(block.toDate) }
    var sessions by remember(block.id) { mutableStateOf(block.sessions) }
    var reason by remember(block.id) { mutableStateOf(block.reason) }
    var appointmentsEnabled by remember(block.id) { mutableStateOf(block.appointmentsEnabled) }
    var error by remember(block.id) { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Outlined.EventBusy, null) },
        title = { Text(if (block.id.isBlank()) "Add availability block" else "Edit availability block") },
        text = {
            Column(
                Modifier.heightIn(max = 520.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    fromDate,
                    { fromDate = it.take(10); error = null },
                    label = { Text("From date") },
                    supportingText = { Text("YYYY-MM-DD") },
                    singleLine = true
                )
                OutlinedTextField(
                    toDate,
                    { toDate = it.take(10); error = null },
                    label = { Text("To date") },
                    supportingText = { Text("YYYY-MM-DD") },
                    singleLine = true
                )
                Text("Affected sessions", fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Morning", "Evening", "Both").forEach { option ->
                        FilterChip(
                            selected = sessions == option,
                            onClick = { sessions = option; error = null },
                            label = { Text(option) }
                        )
                    }
                }
                OutlinedTextField(
                    reason,
                    { reason = it.take(150); error = null },
                    label = { Text("Reason shown to staff") },
                    supportingText = { Text(reason.length.toString() + "/150") },
                    minLines = 2
                )
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Bookings enabled", fontWeight = FontWeight.SemiBold)
                        Text(
                            if (appointmentsEnabled) "Block saved but inactive" else "Block active; new bookings disabled",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                    Switch(appointmentsEnabled, { appointmentsEnabled = it })
                }
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            Button(onClick = {
                error = onSave(
                    block.copy(
                        fromDate = fromDate,
                        toDate = toDate,
                        sessions = sessions,
                        reason = reason,
                        appointmentsEnabled = appointmentsEnabled
                    )
                )
            }) { Text("Save block") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
