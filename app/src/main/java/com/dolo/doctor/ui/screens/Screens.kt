package com.dolo.doctor.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dolo.doctor.auth.AuthUiState
import com.dolo.doctor.data.model.*
import com.dolo.doctor.ui.components.*

@Composable private fun page() = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)

@Composable fun SplashScreen(onContinue: () -> Unit) {
    Box(page().padding(28.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            DoctorBrand()
            Spacer(Modifier.height(30.dp))
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant, shadowElevation = 12.dp, modifier = Modifier.size(150.dp)) {
                Icon(Icons.Outlined.MedicalServices, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(38.dp))
            }
            Spacer(Modifier.height(28.dp))
            Text("Clinic control, simplified.", style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
            Text("Queue, appointments and staff in one place.", color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            Spacer(Modifier.height(34.dp))
            PrimaryAction("Get started", onContinue)
        }
    }
}

@Composable fun LoginScreen(
    state: AuthUiState,
    onRole: (UserRole) -> Unit,
    onPhone: (String) -> Unit,
    onPin: (String) -> Unit,
    onLogin: () -> Unit
) {
    Column(page().imePadding().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.Center) {
        DoctorBrand()
        Spacer(Modifier.height(24.dp))
        Text("Secure clinic access", style = MaterialTheme.typography.headlineLarge)
        Text("Use your individual Doctor or Assistant credentials.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FilterChip(state.selectedRole == UserRole.DOCTOR, { onRole(UserRole.DOCTOR) }, { Text("Doctor") }, leadingIcon = { Icon(Icons.Outlined.MedicalServices, null) }, modifier = Modifier.weight(1f))
            FilterChip(state.selectedRole == UserRole.ASSISTANT, { onRole(UserRole.ASSISTANT) }, { Text("Assistant") }, leadingIcon = { Icon(Icons.Outlined.Badge, null) }, modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(state.phone, onPhone, Modifier.fillMaxWidth(), label = { Text("Mobile number") }, prefix = { Text("+91 ") }, leadingIcon = { Icon(Icons.Outlined.Phone, null) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), singleLine = true, shape = RoundedCornerShape(18.dp))
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(state.pin, onPin, Modifier.fillMaxWidth(), label = { Text("4-digit PIN") }, leadingIcon = { Icon(Icons.Outlined.Lock, null) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword), visualTransformation = PasswordVisualTransformation(), singleLine = true, shape = RoundedCornerShape(18.dp))
        Spacer(Modifier.height(16.dp))
        PrimaryAction("Login as ${state.selectedRole.name.lowercase().replaceFirstChar(Char::uppercase)}", onLogin, enabled = state.phone.length == 10 && state.pin.length == 4, icon = Icons.Outlined.Login)
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 12.dp)) }
        Spacer(Modifier.height(16.dp))
        ElevatedSection("Stage 2 demo credentials") {
            Text("Doctor: 9999999999 • PIN 1234", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Assistant (queue controls): 9876543210 • PIN 1234", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Assistant (view only): 9876501234 • PIN 1234", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
@Composable fun DashboardScreen(
    state: DoctorUiState,
    permissions: Set<Permission>,
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onQueue: () -> Unit,
    onAppointments: () -> Unit,
    onClinic: () -> Unit,
    onAvailability: () -> Unit,
    onAnnouncements: () -> Unit,
    onAssistants: () -> Unit,
    onProfile: () -> Unit,
    onLogout: () -> Unit
) {
    val doctorMode = state.role == UserRole.DOCTOR
    val assistantName = state.assistants.firstOrNull { it.id == state.activeAssistantId }?.name ?: "Assistant"
    val canViewQueue = doctorMode || Permission.VIEW_QUEUE in permissions
    val canViewAppointments = doctorMode || Permission.VIEW_TODAY_APPOINTMENTS in permissions
    var confirmLogout by remember { mutableStateOf(false) }
    Scaffold(containerColor = MaterialTheme.colorScheme.background, bottomBar = { DoctorBottomBar(DoctorBottomDestination.HOME, {}, onQueue, onAppointments, onProfile, profileEnabled = doctorMode) }) { padding ->
        LazyColumn(Modifier.padding(padding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) { DoctorBrand(); Text(if (doctorMode) state.profile.name else assistantName, fontSize = 23.sp, fontWeight = FontWeight.ExtraBold); Text(if (doctorMode) state.profile.specialty else "Assistant • ${permissions.size} permissions", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    IconButton(onToggleTheme) { Icon(if (darkTheme) Icons.Outlined.LightMode else Icons.Outlined.DarkMode, if (darkTheme) "Use light theme" else "Use dark theme") }
                    IconButton(onClick = { confirmLogout = true }) { Icon(Icons.Outlined.Logout, "Logout") }
                }
            }
            item { Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { MetricTile("Current token", state.currentToken.toString(), Modifier.weight(1f), MaterialTheme.colorScheme.error); MetricTile("Waiting", state.appointments.count { it.status in listOf(AppointmentStatus.BOOKED, AppointmentStatus.ARRIVED, AppointmentStatus.WAITING) }.toString(), Modifier.weight(1f), MaterialTheme.colorScheme.tertiary) } }
            item { ElevatedSection("Today's queue", state.clinics.first().name) { Row(verticalAlignment = Alignment.CenterVertically) { StatusPill(state.queueState.name.replace("_", " "), state.queueState == QueueState.ACTIVE); Spacer(Modifier.weight(1f)); Text("Avg. ${state.clinics.first().averageConsultationMinutes} min", color = MaterialTheme.colorScheme.onSurfaceVariant) }; PrimaryAction("Open live queue", onQueue, enabled = canViewQueue, icon = Icons.Outlined.FormatListNumbered) } }
            item { Text("Clinic tools", style = MaterialTheme.typography.titleLarge) }
            item { ToolRow(onAppointments, onClinic, canViewAppointments, doctorMode) }
            if (doctorMode) {
                item { ToolRow(onAvailability, onAnnouncements, true, true, "Availability", "Updates", Icons.Outlined.EventBusy, Icons.Outlined.Campaign) }
                item { ToolRow(onAssistants, onProfile, true, true, "Assistants", "Profile", Icons.Outlined.Groups, Icons.Outlined.Person) }
            } else {
                item { ElevatedSection("Assistant access") { permissions.sortedBy { it.name }.forEach { Text("• ${it.name.replace("_", " ").lowercase().replaceFirstChar(Char::uppercase)}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp) } } }
            }
            if (doctorMode || Permission.MANAGE_ANNOUNCEMENTS in permissions) {
                item { Text("Active doctor updates", style = MaterialTheme.typography.titleLarge) }
                items(state.announcements.filter { it.active }.take(2), key = { it.id }) { AnnouncementCard(it, null) }
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
@Composable private fun ToolRow(first: () -> Unit, second: () -> Unit, firstEnabled: Boolean, secondEnabled: Boolean, firstLabel: String = "Appointments", secondLabel: String = "Clinic", firstIcon: ImageVector = Icons.Outlined.CalendarMonth, secondIcon: ImageVector = Icons.Outlined.Business) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        ToolCard(firstIcon, firstLabel, first, Modifier.weight(1f), firstEnabled)
        ToolCard(secondIcon, secondLabel, second, Modifier.weight(1f), secondEnabled)
    }
}
@Composable private fun ToolCard(icon: ImageVector, label: String, onClick: () -> Unit, modifier: Modifier, enabled: Boolean) {
    Card(modifier.height(112.dp).shadow(8.dp, RoundedCornerShape(22.dp)).clickable(enabled = enabled, onClick = onClick), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(5.dp), shape = RoundedCornerShape(22.dp)) {
        Column(Modifier.fillMaxSize().padding(15.dp), verticalArrangement = Arrangement.SpaceBetween) { Icon(icon, null, tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(32.dp)); Text(label, fontWeight = FontWeight.Bold) }
    }
}

@Composable fun QueueScreen(state: DoctorUiState, permissions: Set<Permission>, onBack: () -> Unit, onHome: () -> Unit, onAppointments: () -> Unit, onProfile: () -> Unit, onToggleQueue: () -> Unit, onCallNext: () -> Unit, onUpdate: (String, AppointmentStatus) -> Unit) {
    val doctorMode = state.role == UserRole.DOCTOR
    val canView = doctorMode || Permission.VIEW_QUEUE in permissions
    val canUpdate = doctorMode || Permission.UPDATE_QUEUE in permissions
    val canCallNext = doctorMode || Permission.CALL_NEXT_PATIENT in permissions
    val canMarkArrived = doctorMode || Permission.MARK_PATIENT_ARRIVED in permissions
    val canMarkAbsent = doctorMode || Permission.MARK_PATIENT_ABSENT in permissions
    Scaffold(containerColor = MaterialTheme.colorScheme.background, bottomBar = { DoctorBottomBar(DoctorBottomDestination.QUEUE, onHome, {}, onAppointments, onProfile, profileEnabled = doctorMode) }) { padding ->
        LazyColumn(Modifier.padding(padding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item { PageHeader("Live queue", onBack) }
            if (!canView) item { ElevatedSection("Access restricted") { Text("This assistant account does not have VIEW_QUEUE permission.", color = MaterialTheme.colorScheme.error) } }
            else {
                item { Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { MetricTile("In consultation", state.currentToken.toString(), Modifier.weight(1f), MaterialTheme.colorScheme.error); MetricTile("Remaining", state.appointments.count { it.token > state.currentToken && it.status !in setOf(AppointmentStatus.ABSENT, AppointmentStatus.COMPLETED) }.toString(), Modifier.weight(1f), MaterialTheme.colorScheme.tertiary) } }
                item { ElevatedSection("Queue controls", "Status: ${state.queueState.name.lowercase().replaceFirstChar(Char::uppercase)}") { Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { Button(onToggleQueue, Modifier.weight(1f), enabled = canUpdate, elevation = ButtonDefaults.buttonElevation(7.dp)) { Text(if (state.queueState == QueueState.ACTIVE) "Pause" else "Resume") }; Button(onCallNext, Modifier.weight(1f), enabled = state.queueState == QueueState.ACTIVE && canCallNext, elevation = ButtonDefaults.buttonElevation(7.dp)) { Text("Call next") } }; if (!canUpdate || !canCallNext) Text("Some controls are disabled by assistant permissions.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp) } }
                items(state.appointments.sortedBy { it.token }, key = { it.id }) { appointment -> QueueAppointmentCard(appointment, canMarkArrived, canMarkAbsent, canUpdate, onUpdate) }
            }
        }
    }
}

@Composable private fun QueueAppointmentCard(appointment: Appointment, canMarkArrived: Boolean, canMarkAbsent: Boolean, canUpdate: Boolean, onUpdate: (String, AppointmentStatus) -> Unit) {
    ElevatedSection("Token ${appointment.token} • ${appointment.patientName}", "${appointment.patientType} • ${appointment.session} • booked ${appointment.bookedAt}") {
        Row(verticalAlignment = Alignment.CenterVertically) { StatusPill(appointment.status.name.replace("_", " "), appointment.status !in setOf(AppointmentStatus.ABSENT, AppointmentStatus.SKIPPED)); Spacer(Modifier.weight(1f)); if (appointment.status == AppointmentStatus.BOOKED && canMarkArrived) TextButton({ onUpdate(appointment.id, AppointmentStatus.ARRIVED) }) { Text("Mark arrived") } }
        if (appointment.status in setOf(AppointmentStatus.ARRIVED, AppointmentStatus.WAITING) && (canMarkAbsent || canUpdate)) Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { if (canMarkAbsent) OutlinedButton({ onUpdate(appointment.id, AppointmentStatus.ABSENT) }, Modifier.weight(1f)) { Text("Absent") }; if (canUpdate) OutlinedButton({ onUpdate(appointment.id, AppointmentStatus.WAITING) }, Modifier.weight(1f)) { Text("Waiting") } }
    }
}
@Composable fun AppointmentsScreen(state: DoctorUiState, permissions: Set<Permission>, onBack: () -> Unit, onHome: () -> Unit, onQueue: () -> Unit, onProfile: () -> Unit) {
    val doctorMode = state.role == UserRole.DOCTOR
    val canView = doctorMode || Permission.VIEW_TODAY_APPOINTMENTS in permissions
    Scaffold(containerColor = MaterialTheme.colorScheme.background, bottomBar = { DoctorBottomBar(DoctorBottomDestination.APPOINTMENTS, onHome, onQueue, {}, onProfile, profileEnabled = doctorMode) }) { padding ->
        LazyColumn(Modifier.padding(padding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item { PageHeader("Today's appointments", onBack) }
            if (!canView) item { ElevatedSection("Access restricted") { Text("This assistant account does not have VIEW_TODAY_APPOINTMENTS permission.", color = MaterialTheme.colorScheme.error) } }
            else {
                item { ElevatedSection("Morning session") { Text("${state.appointments.size} booked patients • maximum ${state.clinics.first().maxTokensPerSession} tokens", color = MaterialTheme.colorScheme.onSurfaceVariant) } }
                items(state.appointments.sortedBy { it.token }, key = { it.id }) { appointment -> ElevatedSection("Token ${appointment.token} • ${appointment.patientName}", "${appointment.patientType} • ${appointment.bookedAt}") { Row { StatusPill(appointment.status.name.replace("_", " "), appointment.status != AppointmentStatus.ABSENT); Spacer(Modifier.weight(1f)); Text(appointment.session, color = MaterialTheme.colorScheme.onSurfaceVariant) } } }
            }
        }
    }
}
@Composable fun ClinicScreen(state: DoctorUiState, onBack: () -> Unit) {
    LazyColumn(page().padding(20.dp), verticalArrangement = Arrangement.spacedBy(15.dp)) {
        item { PageHeader("Clinic & schedule", onBack) }
        items(state.clinics, key = { it.id }) { clinic ->
            ElevatedSection(clinic.name, clinic.address) {
                DetailLine(Icons.Outlined.Phone, clinic.phone)
                DetailLine(Icons.Outlined.LightMode, "Morning: ${clinic.morningSession}")
                DetailLine(Icons.Outlined.DarkMode, "Evening: ${clinic.eveningSession}")
                DetailLine(Icons.Outlined.ConfirmationNumber, "${clinic.maxTokensPerSession} tokens per session")
                PrimaryAction("Edit clinic schedule", {}, icon = Icons.Outlined.CalendarMonth)
            }
        }
    }
}

@Composable fun AvailabilityScreen(state: DoctorUiState, onBack: () -> Unit, onToggle: (String) -> Unit) {
    LazyColumn(page().padding(20.dp), verticalArrangement = Arrangement.spacedBy(15.dp)) {
        item { PageHeader("Availability", onBack) }
        item { ElevatedSection("Booking control", "Disable appointments for a date, session or date range.") { PrimaryAction("Add availability block", {}, icon = Icons.Outlined.EventBusy) } }
        items(state.availabilityBlocks, key = { it.id }) { block -> ElevatedSection("${block.fromDate} - ${block.toDate}", "${block.sessions} • ${block.reason}") { Row(verticalAlignment = Alignment.CenterVertically) { StatusPill(if (block.appointmentsEnabled) "Appointments enabled" else "Appointments disabled", block.appointmentsEnabled); Spacer(Modifier.weight(1f)); Switch(block.appointmentsEnabled, { onToggle(block.id) }) } } }
    }
}

@Composable fun AnnouncementsScreen(state: DoctorUiState, onBack: () -> Unit, onToggle: (String) -> Unit) {
    LazyColumn(page().padding(20.dp), verticalArrangement = Arrangement.spacedBy(15.dp)) {
        item { PageHeader("Doctor updates", onBack) }
        item { ElevatedSection("Patient profile feed", "Availability notices, camps and offers appear under the doctor's Patient App profile.") { PrimaryAction("Create announcement", {}, icon = Icons.Outlined.Add) } }
        items(state.announcements, key = { it.id }) { AnnouncementCard(it) { onToggle(it.id) } }
    }
}

@Composable private fun AnnouncementCard(announcement: Announcement, onToggle: (() -> Unit)?) {
    val icon = when (announcement.type) { AnnouncementType.CAMP -> Icons.Outlined.HealthAndSafety; AnnouncementType.OFFER -> Icons.Outlined.LocalOffer; AnnouncementType.AVAILABILITY -> Icons.Outlined.EventBusy; AnnouncementType.GENERAL -> Icons.Outlined.Campaign }
    ElevatedSection(announcement.title, "${announcement.startsOn} - ${announcement.endsOn}") {
        Row { Icon(icon, null, tint = MaterialTheme.colorScheme.primary); Spacer(Modifier.width(10.dp)); Text(announcement.message, Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant) }
        Row(verticalAlignment = Alignment.CenterVertically) { StatusPill(if (announcement.active) "Visible to patients" else "Hidden", announcement.active); if (onToggle != null) { Spacer(Modifier.weight(1f)); Switch(announcement.active, { onToggle() }) } }
    }
}

@Composable fun AssistantsScreen(
    state: DoctorUiState,
    onBack: () -> Unit,
    onTogglePermission: (String, Permission) -> Unit,
    onDeleteAssistant: (String) -> Unit
) {
    var pendingDeletion by remember { mutableStateOf<Assistant?>(null) }

    LazyColumn(page().padding(20.dp), verticalArrangement = Arrangement.spacedBy(15.dp)) {
        item { PageHeader("Assistants", onBack) }
        item { ElevatedSection("Staff access", "Each assistant uses individual credentials and backend-enforced permissions.") { PrimaryAction("Add assistant", {}, icon = Icons.Outlined.PersonAdd) } }
        items(state.assistants, key = { it.id }) { assistant ->
            ElevatedSection(assistant.name, assistant.phone) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    StatusPill(if (assistant.active) "Active" else "Disabled", assistant.active)
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = { pendingDeletion = assistant }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                        Icon(Icons.Outlined.DeleteOutline, null)
                        Spacer(Modifier.width(5.dp))
                        Text("Delete")
                    }
                }
                Permission.entries.forEach { permission ->
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(permission.name.replace("_", " ").lowercase().replaceFirstChar(Char::uppercase), Modifier.weight(1f), fontSize = 12.sp)
                        Switch(permission in assistant.permissions, { onTogglePermission(assistant.id, permission) })
                    }
                }
            }
        }
        if (state.assistants.isEmpty()) {
            item { ElevatedSection("No assistants") { Text("Add an assistant when clinic staff access is needed.", color = MaterialTheme.colorScheme.onSurfaceVariant) } }
        }
    }

    pendingDeletion?.let { assistant ->
        AlertDialog(
            onDismissRequest = { pendingDeletion = null },
            icon = { Icon(Icons.Outlined.PersonRemove, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Delete ${assistant.name}?") },
            text = { Text("This removes the assistant profile and its permissions from the local clinic workspace. This action cannot be undone in this prototype without clearing the app data.") },
            confirmButton = {
                TextButton(onClick = { onDeleteAssistant(assistant.id); pendingDeletion = null }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text("Delete assistant") }
            },
            dismissButton = { TextButton(onClick = { pendingDeletion = null }) { Text("Keep assistant") } }
        )
    }
}
@Composable fun ProfileScreen(state: DoctorUiState, onBack: () -> Unit, onHome: () -> Unit, onQueue: () -> Unit, onAppointments: () -> Unit) {
    Scaffold(containerColor = MaterialTheme.colorScheme.background, bottomBar = { DoctorBottomBar(DoctorBottomDestination.PROFILE, onHome, onQueue, onAppointments, {}) }) { padding ->
        LazyColumn(Modifier.padding(padding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(15.dp)) {
            item { PageHeader("Doctor profile", onBack) }
            item { ElevatedSection(state.profile.name, state.profile.specialty) { Row(verticalAlignment = Alignment.CenterVertically) { StatusPill(if (state.profile.verified) "Verified profile" else "Verification pending", state.profile.verified); Spacer(Modifier.weight(1f)); Icon(Icons.Outlined.Verified, null, tint = MaterialTheme.colorScheme.primary) }; DetailLine(Icons.Outlined.School, state.profile.qualification); DetailLine(Icons.Outlined.Badge, state.profile.registrationNumber); DetailLine(Icons.Outlined.WorkHistory, "${state.profile.experienceYears} years experience"); DetailLine(Icons.Outlined.Payments, "₹${state.profile.consultationFee} consultation fee") } }
            item { ElevatedSection("About") { Text(state.profile.about, color = MaterialTheme.colorScheme.onSurfaceVariant); PrimaryAction("Edit profile information", {}, icon = Icons.Outlined.Edit) } }
            item { ElevatedSection("Verification rule") { Text("Changes to registration number, specialty or credentials will require Admin review.", color = MaterialTheme.colorScheme.onSurfaceVariant) } }
        }
    }
}

@Composable private fun DetailLine(icon: ImageVector, text: String) { Row(verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = MaterialTheme.colorScheme.primary); Spacer(Modifier.width(11.dp)); Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant) } }