package com.dolo.doctor.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dolo.doctor.data.model.*
import com.dolo.doctor.ui.components.*

@Composable
fun AnnouncementManagementScreen(
    state: DoctorUiState,
    onBack: () -> Unit,
    onSave: (Announcement) -> String?,
    onSetActive: (String, Boolean) -> Boolean,
    onDelete: (String) -> Boolean
) {
    var editing by remember { mutableStateOf<Announcement?>(null) }
    var deleting by remember { mutableStateOf<Announcement?>(null) }
    val statuses = state.announcements.associateWith { publicationStatus(it, state.queueDate) }

    LazyColumn(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        item { PageHeader("Doctor updates", onBack) }
        item {
            ElevatedSection("Patient profile feed", "Publish availability notices, health camps, offers and general clinic updates.") {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricTile("Live", statuses.values.count { it == AnnouncementPublicationStatus.LIVE }.toString(), Modifier.weight(1f), MaterialTheme.colorScheme.primary)
                    MetricTile("Scheduled", statuses.values.count { it == AnnouncementPublicationStatus.SCHEDULED }.toString(), Modifier.weight(1f), MaterialTheme.colorScheme.tertiary)
                }
                Text("Only published updates within their start and end dates appear in the future Patient App profile feed.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                PrimaryAction("Create doctor update", {
                    editing = Announcement("", "", "", AnnouncementType.GENERAL, state.queueDate, state.queueDate, false)
                }, icon = Icons.Outlined.Add)
            }
        }

        if (state.announcements.isEmpty()) {
            item { ElevatedSection("No doctor updates") { Text("Create an update when patients need clinic news.", color = MaterialTheme.colorScheme.onSurfaceVariant) } }
        }

        items(state.announcements.sortedWith(compareByDescending<Announcement> { it.startsOn }.thenBy { it.title }), key = { it.id }) { announcement ->
            val status = statuses[announcement] ?: AnnouncementPublicationStatus.DRAFT
            AnnouncementManagementCard(
                announcement,
                status,
                { onSetActive(announcement.id, it) },
                { editing = announcement },
                { deleting = announcement }
            )
        }
    }

    editing?.let { announcement ->
        AnnouncementEditDialog(announcement, { editing = null }) {
            val result = onSave(it)
            if (result == null) editing = null
            result
        }
    }

    deleting?.let { announcement ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            icon = { Icon(Icons.Outlined.DeleteOutline, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Delete doctor update?") },
            text = { Text("'" + announcement.title + "' will be removed from this device and the future Patient App feed.") },
            confirmButton = {
                TextButton(
                    onClick = { if (onDelete(announcement.id)) deleting = null },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text("Keep update") } }
        )
    }
}

@Composable
private fun AnnouncementManagementCard(
    announcement: Announcement,
    status: AnnouncementPublicationStatus,
    onSetActive: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    ElevatedSection(announcement.title, announcement.startsOn + " to " + announcement.endsOn) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Icon(announcementIcon(announcement.type), null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(announcement.type.name.lowercase().replaceFirstChar(Char::uppercase), fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                Text(announcement.message, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            StatusPill(status.name.lowercase().replaceFirstChar(Char::uppercase), status in setOf(AnnouncementPublicationStatus.LIVE, AnnouncementPublicationStatus.SCHEDULED))
            Spacer(Modifier.weight(1f))
            Text(if (announcement.active) "Published" else "Draft", fontSize = 12.sp)
            Spacer(Modifier.width(6.dp))
            Switch(announcement.active, onSetActive)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onEdit) {
                Icon(Icons.Outlined.Edit, null)
                Spacer(Modifier.width(4.dp))
                Text("Edit")
            }
            TextButton(onClick = onDelete, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                Icon(Icons.Outlined.DeleteOutline, null)
                Spacer(Modifier.width(4.dp))
                Text("Delete")
            }
        }
    }
}

@Composable
private fun AnnouncementEditDialog(
    announcement: Announcement,
    onDismiss: () -> Unit,
    onSave: (Announcement) -> String?
) {
    var title by remember(announcement.id) { mutableStateOf(announcement.title) }
    var message by remember(announcement.id) { mutableStateOf(announcement.message) }
    var type by remember(announcement.id) { mutableStateOf(announcement.type) }
    var startsOn by remember(announcement.id) { mutableStateOf(announcement.startsOn) }
    var endsOn by remember(announcement.id) { mutableStateOf(announcement.endsOn) }
    var active by remember(announcement.id) { mutableStateOf(announcement.active) }
    var error by remember(announcement.id) { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(announcementIcon(type), null) },
        title = { Text(if (announcement.id.isBlank()) "Create doctor update" else "Edit doctor update") },
        text = {
            Column(Modifier.heightIn(max = 540.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Update type", fontWeight = FontWeight.SemiBold)
                AnnouncementType.entries.chunked(2).forEach { rowTypes ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        rowTypes.forEach { option ->
                            FilterChip(
                                selected = type == option,
                                onClick = { type = option; error = null },
                                label = { Text(option.name.lowercase().replaceFirstChar(Char::uppercase)) },
                                leadingIcon = { Icon(announcementIcon(option), null) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                OutlinedTextField(title, { title = it.take(80); error = null }, label = { Text("Title") }, supportingText = { Text(title.length.toString() + "/80") }, singleLine = true)
                OutlinedTextField(message, { message = it.take(300); error = null }, label = { Text("Message shown to patients") }, supportingText = { Text(message.length.toString() + "/300") }, minLines = 3)
                OutlinedTextField(startsOn, { startsOn = it.take(10); error = null }, label = { Text("Start date") }, supportingText = { Text("YYYY-MM-DD") }, singleLine = true)
                OutlinedTextField(endsOn, { endsOn = it.take(10); error = null }, label = { Text("End date") }, supportingText = { Text("YYYY-MM-DD") }, singleLine = true)
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Publish update", fontWeight = FontWeight.SemiBold)
                        Text(if (active) "Visible only during the selected date range" else "Saved as draft", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    }
                    Switch(active, { active = it })
                }
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            Button(onClick = {
                error = onSave(announcement.copy(title = title, message = message, type = type, startsOn = startsOn, endsOn = endsOn, active = active))
            }) { Text("Save update") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

private fun publicationStatus(announcement: Announcement, date: String): AnnouncementPublicationStatus = when {
    !announcement.active -> AnnouncementPublicationStatus.DRAFT
    date < announcement.startsOn -> AnnouncementPublicationStatus.SCHEDULED
    date > announcement.endsOn -> AnnouncementPublicationStatus.EXPIRED
    else -> AnnouncementPublicationStatus.LIVE
}

private fun announcementIcon(type: AnnouncementType): ImageVector = when (type) {
    AnnouncementType.AVAILABILITY -> Icons.Outlined.EventBusy
    AnnouncementType.CAMP -> Icons.Outlined.HealthAndSafety
    AnnouncementType.OFFER -> Icons.Outlined.LocalOffer
    AnnouncementType.GENERAL -> Icons.Outlined.Campaign
}
