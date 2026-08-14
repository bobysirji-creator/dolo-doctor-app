package com.dolo.doctor.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dolo.doctor.data.model.DoctorUiState
import com.dolo.doctor.data.model.Permission
import com.dolo.doctor.data.model.UserRole
import com.dolo.doctor.ui.components.DoctorBottomBar
import com.dolo.doctor.ui.components.DoctorBrand
import com.dolo.doctor.ui.components.DoctorBottomDestination
import com.dolo.doctor.ui.navigation.DoctorMoreDestination
import com.dolo.doctor.ui.navigation.DoctorMoreGroup
import com.dolo.doctor.ui.navigation.DoctorNavigationPolicy

@Composable
fun DoctorMoreScreen(
    state: DoctorUiState,
    permissions: Set<Permission>,
    darkTheme: Boolean,
    unreadNotifications: Int,
    onToggleTheme: () -> Unit,
    onToday: () -> Unit,
    onAppointments: () -> Unit,
    onClinic: () -> Unit,
    onOpen: (DoctorMoreDestination) -> Unit,
    onLogout: () -> Unit
) {
    val doctorMode = state.role == UserRole.DOCTOR
    val assistantName = state.assistants.firstOrNull { it.id == state.activeAssistantId }?.name ?: "Assistant"
    val destinations = DoctorNavigationPolicy.visibleMoreDestinations(state.role, permissions, state.activeAssistantId)
    var confirmLogout by remember { mutableStateOf(false) }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            DoctorBottomBar(
                selected = DoctorBottomDestination.MORE,
                onHome = onToday,
                onAppointments = onAppointments,
                onClinic = onClinic,
                clinicEnabled = DoctorNavigationPolicy.canOpenClinic(state.role, permissions)
            )
        }
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding).padding(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        DoctorBrand()
                        Text(if (doctorMode) state.profile.name else assistantName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                        Text(if (doctorMode) "Doctor account" else "Assistant • ${permissions.size} permissions", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onToggleTheme) {
                        Icon(if (darkTheme) Icons.Outlined.LightMode else Icons.Outlined.DarkMode, if (darkTheme) "Use light theme" else "Use dark theme")
                    }
                }
            }
            DoctorMoreGroup.entries.forEach { group ->
                val groupItems = destinations.filter { it.group == group }
                if (groupItems.isNotEmpty()) {
                    item(group.name) { Text(group.label, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
                    items(groupItems, key = { it.name }) { destination ->
                        DoctorMoreRow(
                            destination = destination,
                            badge = if (destination == DoctorMoreDestination.NOTIFICATIONS) unreadNotifications else 0,
                            onClick = { onOpen(destination) }
                        )
                    }
                }
            }
            if (!doctorMode) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Text(
                            "Only actions allowed by this Assistant account are shown. Doctor-only profile, staff, backup and audit controls remain hidden.",
                            Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            item {
                OutlinedButton(onClick = { confirmLogout = true }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Outlined.Logout, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Logout")
                }
            }
        }
    }
    if (confirmLogout) {
        AlertDialog(
            onDismissRequest = { confirmLogout = false },
            icon = { Icon(Icons.Outlined.Logout, null) },
            title = { Text("Logout from DO-LO Doctor?") },
            text = { Text("Your saved session will be cleared only after you confirm logout.") },
            confirmButton = { TextButton(onClick = { confirmLogout = false; onLogout() }) { Text("Logout") } },
            dismissButton = { TextButton(onClick = { confirmLogout = false }) { Text("Stay logged in") } }
        )
    }
}

@Composable
private fun DoctorMoreRow(destination: DoctorMoreDestination, badge: Int, onClick: () -> Unit) {
    val icon: ImageVector = when (destination) {
        DoctorMoreDestination.NOTIFICATIONS -> Icons.Outlined.Notifications
        DoctorMoreDestination.PROFILE -> Icons.Outlined.Person
        DoctorMoreDestination.CHANGE_PIN -> Icons.Outlined.LockReset
        DoctorMoreDestination.AVAILABILITY -> Icons.Outlined.EventBusy
        DoctorMoreDestination.ANNOUNCEMENTS -> Icons.Outlined.Campaign
        DoctorMoreDestination.ASSISTANTS -> Icons.Outlined.Groups
        DoctorMoreDestination.REPORTS -> Icons.Outlined.Insights
        DoctorMoreDestination.HISTORY -> Icons.Outlined.History
        DoctorMoreDestination.ACTIVITY -> Icons.Outlined.FactCheck
        DoctorMoreDestination.HOSTED_SYNC -> Icons.Outlined.Cloud
        DoctorMoreDestination.SYNC -> Icons.Outlined.CloudSync
        DoctorMoreDestination.BACKUP -> Icons.Outlined.Backup
    }
    ElevatedCard(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        ListItem(
            headlineContent = { Text(destination.label, fontWeight = FontWeight.Bold) },
            supportingContent = { Text(destination.description) },
            leadingContent = { Icon(icon, null, tint = MaterialTheme.colorScheme.primary) },
            trailingContent = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (badge > 0) Badge { Text(badge.coerceAtMost(99).toString()) }
                    Icon(Icons.Outlined.ChevronRight, null)
                }
            }
        )
    }
}