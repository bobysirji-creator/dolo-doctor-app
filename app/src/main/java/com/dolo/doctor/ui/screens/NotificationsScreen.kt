package com.dolo.doctor.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dolo.doctor.data.model.AuditAction
import com.dolo.doctor.data.model.DoctorUiState
import com.dolo.doctor.data.model.QueueAuditEvent
import com.dolo.doctor.ui.components.ElevatedSection
import com.dolo.doctor.ui.components.PageHeader

@Composable
fun NotificationsScreen(
    state: DoctorUiState,
    onBack: () -> Unit,
    onRead: (Int) -> Unit,
    onMarkAllRead: () -> Unit
) {
    val notifications = state.auditEvents.sortedByDescending { it.sequence }
    val unreadCount = notifications.count { it.sequence > state.notificationReadThrough }

    LazyColumn(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { PageHeader("Notifications", onBack) }
        item {
            ElevatedSection("Clinic activity alerts", "Queue, appointment, fee and session events are retained on this device.") {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("$unreadCount unread", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = onMarkAllRead, enabled = unreadCount > 0) { Text("Mark all read") }
                }
                Text("Remote Patient App events and Android push alerts will connect through the shared backend later.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }
        }
        if (notifications.isEmpty()) {
            item { ElevatedSection("No notifications yet") { Text("New queue and appointment activity will appear here.") } }
        }
        items(notifications, key = { it.id }) { event ->
            NotificationCard(event, event.sequence > state.notificationReadThrough) { onRead(event.sequence) }
        }
    }
}

@Composable
private fun NotificationCard(event: QueueAuditEvent, unread: Boolean, onRead: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onRead),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (unread) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(if (unread) 7.dp else 2.dp)
    ) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.Top) {
            Icon(Icons.Outlined.NotificationsActive, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(notificationTitle(event.action), Modifier.weight(1f), fontWeight = FontWeight.Bold)
                    if (unread) Badge()
                }
                Text(event.detail, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(event.date + " at " + event.time + " by " + event.actor, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                if (event.token != null) Text("Token " + event.token + (event.patientName?.let { " ? " + it } ?: ""), fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
            }
        }
    }
}

private fun notificationTitle(action: AuditAction): String = when (action) {
    AuditAction.WALK_IN_BOOKED -> "Walk-in appointment booked"
    AuditAction.FEE_CONFIRMED -> "Consultation fee confirmed"
    AuditAction.RECEIPT_GENERATED -> "Token receipt generated"
    AuditAction.PATIENT_CALLED -> "Patient called"
    AuditAction.CONSULTATION_COMPLETED -> "Consultation completed"
    AuditAction.QUEUE_STARTED -> "Queue started"
    AuditAction.QUEUE_PAUSED -> "Queue paused"
    AuditAction.QUEUE_RESUMED -> "Queue resumed"
    AuditAction.SESSION_CLOSED -> "Session closed"
    AuditAction.DAY_CLOSED -> "Day archived"
    AuditAction.DAY_ROLLED_OVER -> "New clinic day"
    AuditAction.PATIENT_REJOINED -> "Patient rejoined"
    AuditAction.STATUS_CHANGED -> "Appointment updated"
    AuditAction.AVAILABILITY_SAVED -> "Availability block saved"
    AuditAction.AVAILABILITY_CHANGED -> "Booking availability changed"
    AuditAction.AVAILABILITY_DELETED -> "Availability block deleted"
    AuditAction.AFFECTED_PATIENT_UPDATED -> "Affected patient updated"
    AuditAction.ANNOUNCEMENT_SAVED -> "Doctor update saved"
    AuditAction.ANNOUNCEMENT_VISIBILITY_CHANGED -> "Doctor update visibility changed"
    AuditAction.ANNOUNCEMENT_DELETED -> "Doctor update deleted"
}
