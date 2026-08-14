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
import com.dolo.doctor.hosted.HostedStaffNotification
import com.dolo.doctor.hosted.HostedStaffUiState
import com.dolo.doctor.ui.components.ElevatedSection
import com.dolo.doctor.ui.components.PageHeader

@Composable
fun NotificationsScreen(
    hostedState: HostedStaffUiState,
    onBack: () -> Unit,
    onMarkHostedRead: (String) -> Unit
) {
    val notifications = hostedState.snapshot?.notifications.orEmpty()
    val newestCursor = notifications
        .maxByOrNull { runCatching { it.cursor.toLong() }.getOrDefault(0L) }
        ?.cursor
    val unreadCount = notifications.count { !it.read }

    LazyColumn(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).safeDrawingPadding().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { PageHeader("Notifications", onBack) }
        item {
            ElevatedSection("Actual notifications", "Important clinic and hosted service messages only.") {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("$unreadCount unread", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.weight(1f))
                    TextButton(
                        onClick = { newestCursor?.let(onMarkHostedRead) },
                        enabled = unreadCount > 0 && newestCursor != null
                    ) { Text("Mark all read") }
                }
                Text(
                    "Routine queue, appointment, fee, receipt and configuration actions remain available in Activity log and are not notifications.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
        }
        if (notifications.isEmpty()) {
            item {
                ElevatedSection("No notifications") {
                    Text("Important Doctor or clinic notifications will appear here.")
                }
            }
        } else {
            items(notifications, key = { "hosted-${it.cursor}" }) { event ->
                HostedNotificationCard(event, !event.read) { onMarkHostedRead(event.cursor) }
            }
        }
    }
}

@Composable
private fun HostedNotificationCard(event: HostedStaffNotification, unread: Boolean, onRead: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onRead),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (unread) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(if (unread) 7.dp else 2.dp)
    ) {
        Column(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.NotificationsActive, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(10.dp))
                Text(event.title, Modifier.weight(1f), fontWeight = FontWeight.Bold)
                if (unread) Badge()
            }
            Text(event.message, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                "${event.patientName} • Token ${event.tokenNumber}",
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp
            )
        }
    }
}
