package com.dolo.doctor.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dolo.doctor.data.model.*
import com.dolo.doctor.ui.components.*
import com.dolo.doctor.ui.theme.*

private val page = Modifier.fillMaxSize().background(DoctorBackground)

@Composable fun SplashScreen(onContinue: () -> Unit) {
    Box(page.padding(28.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            DoctorBrand()
            Spacer(Modifier.height(30.dp))
            Surface(shape = CircleShape, color = DoctorSurfaceAlt, shadowElevation = 12.dp, modifier = Modifier.size(150.dp)) {
                Icon(Icons.Outlined.MedicalServices, null, tint = DoctorTeal, modifier = Modifier.padding(38.dp))
            }
            Spacer(Modifier.height(28.dp))
            Text("Clinic control, simplified.", style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
            Text("Queue, appointments and staff in one place.", color = DoctorMuted, textAlign = TextAlign.Center)
            Spacer(Modifier.height(34.dp))
            PrimaryAction("Get started", onContinue)
        }
    }
}

@Composable fun LoginScreen(onLogin: (UserRole) -> Unit) {
    Column(page.padding(24.dp), verticalArrangement = Arrangement.Center) {
        DoctorBrand()
        Spacer(Modifier.height(28.dp))
        Text("Welcome back", style = MaterialTheme.typography.headlineLarge)
        Text("Choose how you want to access the clinic.", color = DoctorMuted)
        Spacer(Modifier.height(24.dp))
        RoleCard(Icons.Outlined.MedicalServices, "Doctor login", "Full clinic, staff, announcements and profile access") { onLogin(UserRole.DOCTOR) }
        Spacer(Modifier.height(16.dp))
        RoleCard(Icons.Outlined.Badge, "Assistant login", "Permission-limited queue and appointment access") { onLogin(UserRole.ASSISTANT) }
        Spacer(Modifier.height(18.dp))
        ElevatedSection("Stage 1 demo access", "Authentication is local and contains no real credentials.") {
            Text("Secure OTP/password and backend sessions will be connected later.", color = DoctorMuted)
        }
    }
}

@Composable private fun RoleCard(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().shadow(10.dp, RoundedCornerShape(24.dp)).clickable(onClick = onClick), colors = CardDefaults.cardColors(containerColor = Color(0xFFF2F9FF)), elevation = CardDefaults.cardElevation(6.dp), shape = RoundedCornerShape(24.dp)) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = Color.White, shadowElevation = 6.dp) { Icon(icon, null, tint = DoctorTeal, modifier = Modifier.padding(15.dp).size(32.dp)) }
            Spacer(Modifier.width(15.dp))
            Column(Modifier.weight(1f)) { Text(title, fontWeight = FontWeight.ExtraBold, fontSize = 19.sp); Text(subtitle, color = DoctorMuted, fontSize = 13.sp) }
            Icon(Icons.Outlined.ArrowForward, null, tint = DoctorTeal)
        }
    }
}

@Composable fun DashboardScreen(
    state: DoctorUiState,
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
    Scaffold(containerColor = DoctorBackground, bottomBar = { DoctorBottomBar(DoctorBottomDestination.HOME, {}, onQueue, onAppointments, onProfile) }) { padding ->
        LazyColumn(Modifier.padding(padding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) { DoctorBrand(); Text(if (doctorMode) state.profile.name else "Assistant workspace", fontSize = 23.sp, fontWeight = FontWeight.ExtraBold); Text(if (doctorMode) state.profile.specialty else "Permission-limited clinic access", color = DoctorMuted) }
                    IconButton(onLogout) { Icon(Icons.Outlined.Logout, "Logout") }
                }
            }
            item { Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { MetricTile("Current token", state.currentToken.toString(), Modifier.weight(1f), DoctorCoral); MetricTile("Waiting", state.appointments.count { it.status in listOf(AppointmentStatus.BOOKED, AppointmentStatus.ARRIVED, AppointmentStatus.WAITING) }.toString(), Modifier.weight(1f), DoctorBlue) } }
            item { ElevatedSection("Today's queue", state.clinics.first().name) { Row(verticalAlignment = Alignment.CenterVertically) { StatusPill(state.queueState.name.replace("_", " "), state.queueState == QueueState.ACTIVE); Spacer(Modifier.weight(1f)); Text("Avg. ${state.clinics.first().averageConsultationMinutes} min", color = DoctorMuted) }; PrimaryAction("Open live queue", onQueue, icon = Icons.Outlined.FormatListNumbered) } }
            item { Text("Clinic tools", style = MaterialTheme.typography.titleLarge) }
            item { ToolRow(onAppointments, onClinic, doctorMode) }
            if (doctorMode) {
                item { ToolRow(onAvailability, onAnnouncements, true, "Availability", "Updates", Icons.Outlined.EventBusy, Icons.Outlined.Campaign) }
                item { ToolRow(onAssistants, onProfile, true, "Assistants", "Profile", Icons.Outlined.Groups, Icons.Outlined.Person) }
            } else {
                item { ElevatedSection("Assistant access") { Text("Only queue and appointment tools are shown. Doctor controls remain protected by backend permissions in future stages.", color = DoctorMuted) } }
            }
            item { Text("Active doctor updates", style = MaterialTheme.typography.titleLarge) }
            items(state.announcements.filter { it.active }.take(2), key = { it.id }) { AnnouncementCard(it, null) }
        }
    }
}

@Composable private fun ToolRow(first: () -> Unit, second: () -> Unit, enabled: Boolean, firstLabel: String = "Appointments", secondLabel: String = "Clinic", firstIcon: ImageVector = Icons.Outlined.CalendarMonth, secondIcon: ImageVector = Icons.Outlined.Business) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        ToolCard(firstIcon, firstLabel, first, Modifier.weight(1f), enabled)
        ToolCard(secondIcon, secondLabel, second, Modifier.weight(1f), enabled)
    }
}

@Composable private fun ToolCard(icon: ImageVector, label: String, onClick: () -> Unit, modifier: Modifier, enabled: Boolean) {
    Card(modifier.height(112.dp).shadow(8.dp, RoundedCornerShape(22.dp)).clickable(enabled = enabled, onClick = onClick), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(5.dp), shape = RoundedCornerShape(22.dp)) {
        Column(Modifier.fillMaxSize().padding(15.dp), verticalArrangement = Arrangement.SpaceBetween) { Icon(icon, null, tint = if (enabled) DoctorTeal else DoctorMuted, modifier = Modifier.size(32.dp)); Text(label, fontWeight = FontWeight.Bold) }
    }
}

@Composable fun QueueScreen(state: DoctorUiState, onBack: () -> Unit, onHome: () -> Unit, onAppointments: () -> Unit, onProfile: () -> Unit, onToggleQueue: () -> Unit, onCallNext: () -> Unit, onUpdate: (String, AppointmentStatus) -> Unit) {
    Scaffold(containerColor = DoctorBackground, bottomBar = { DoctorBottomBar(DoctorBottomDestination.QUEUE, onHome, {}, onAppointments, onProfile) }) { padding ->
        LazyColumn(Modifier.padding(padding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item { PageHeader("Live queue", onBack) }
            item { Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { MetricTile("In consultation", state.currentToken.toString(), Modifier.weight(1f), DoctorCoral); MetricTile("Remaining", state.appointments.count { it.token > state.currentToken && it.status !in setOf(AppointmentStatus.ABSENT, AppointmentStatus.COMPLETED) }.toString(), Modifier.weight(1f), DoctorBlue) } }
            item { ElevatedSection("Queue controls", "Status: ${state.queueState.name.lowercase().replaceFirstChar(Char::uppercase)}") { Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { Button(onToggleQueue, Modifier.weight(1f), elevation = ButtonDefaults.buttonElevation(7.dp)) { Text(if (state.queueState == QueueState.ACTIVE) "Pause" else "Resume") }; Button(onCallNext, Modifier.weight(1f), enabled = state.queueState == QueueState.ACTIVE, elevation = ButtonDefaults.buttonElevation(7.dp)) { Text("Call next") } } } }
            items(state.appointments.sortedBy { it.token }, key = { it.id }) { appointment -> QueueAppointmentCard(appointment, onUpdate) }
        }
    }
}

@Composable private fun QueueAppointmentCard(appointment: Appointment, onUpdate: (String, AppointmentStatus) -> Unit) {
    ElevatedSection("Token ${appointment.token} • ${appointment.patientName}", "${appointment.patientType} • ${appointment.session} • booked ${appointment.bookedAt}") {
        Row(verticalAlignment = Alignment.CenterVertically) { StatusPill(appointment.status.name.replace("_", " "), appointment.status !in setOf(AppointmentStatus.ABSENT, AppointmentStatus.SKIPPED)); Spacer(Modifier.weight(1f)); if (appointment.status == AppointmentStatus.BOOKED) TextButton({ onUpdate(appointment.id, AppointmentStatus.ARRIVED) }) { Text("Mark arrived") } }
        if (appointment.status in setOf(AppointmentStatus.ARRIVED, AppointmentStatus.WAITING)) Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { OutlinedButton({ onUpdate(appointment.id, AppointmentStatus.ABSENT) }, Modifier.weight(1f)) { Text("Absent") }; OutlinedButton({ onUpdate(appointment.id, AppointmentStatus.WAITING) }, Modifier.weight(1f)) { Text("Waiting") } }
    }
}

@Composable fun AppointmentsScreen(state: DoctorUiState, onBack: () -> Unit, onHome: () -> Unit, onQueue: () -> Unit, onProfile: () -> Unit) {
    Scaffold(containerColor = DoctorBackground, bottomBar = { DoctorBottomBar(DoctorBottomDestination.APPOINTMENTS, onHome, onQueue, {}, onProfile) }) { padding ->
        LazyColumn(Modifier.padding(padding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item { PageHeader("Today's appointments", onBack) }
            item { ElevatedSection("Morning session") { Text("${state.appointments.size} booked patients • maximum ${state.clinics.first().maxTokensPerSession} tokens", color = DoctorMuted) } }
            items(state.appointments.sortedBy { it.token }, key = { it.id }) { appointment -> ElevatedSection("Token ${appointment.token} • ${appointment.patientName}", "${appointment.patientType} • ${appointment.bookedAt}") { Row { StatusPill(appointment.status.name.replace("_", " "), appointment.status != AppointmentStatus.ABSENT); Spacer(Modifier.weight(1f)); Text(appointment.session, color = DoctorMuted) } } }
        }
    }
}

@Composable fun ClinicScreen(state: DoctorUiState, onBack: () -> Unit) {
    LazyColumn(page.padding(20.dp), verticalArrangement = Arrangement.spacedBy(15.dp)) {
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
    LazyColumn(page.padding(20.dp), verticalArrangement = Arrangement.spacedBy(15.dp)) {
        item { PageHeader("Availability", onBack) }
        item { ElevatedSection("Booking control", "Disable appointments for a date, session or date range.") { PrimaryAction("Add availability block", {}, icon = Icons.Outlined.EventBusy) } }
        items(state.availabilityBlocks, key = { it.id }) { block -> ElevatedSection("${block.fromDate} - ${block.toDate}", "${block.sessions} • ${block.reason}") { Row(verticalAlignment = Alignment.CenterVertically) { StatusPill(if (block.appointmentsEnabled) "Appointments enabled" else "Appointments disabled", block.appointmentsEnabled); Spacer(Modifier.weight(1f)); Switch(block.appointmentsEnabled, { onToggle(block.id) }) } } }
    }
}

@Composable fun AnnouncementsScreen(state: DoctorUiState, onBack: () -> Unit, onToggle: (String) -> Unit) {
    LazyColumn(page.padding(20.dp), verticalArrangement = Arrangement.spacedBy(15.dp)) {
        item { PageHeader("Doctor updates", onBack) }
        item { ElevatedSection("Patient profile feed", "Availability notices, camps and offers appear under the doctor's Patient App profile.") { PrimaryAction("Create announcement", {}, icon = Icons.Outlined.Add) } }
        items(state.announcements, key = { it.id }) { AnnouncementCard(it) { onToggle(it.id) } }
    }
}

@Composable private fun AnnouncementCard(announcement: Announcement, onToggle: (() -> Unit)?) {
    val icon = when (announcement.type) { AnnouncementType.CAMP -> Icons.Outlined.HealthAndSafety; AnnouncementType.OFFER -> Icons.Outlined.LocalOffer; AnnouncementType.AVAILABILITY -> Icons.Outlined.EventBusy; AnnouncementType.GENERAL -> Icons.Outlined.Campaign }
    ElevatedSection(announcement.title, "${announcement.startsOn} - ${announcement.endsOn}") {
        Row { Icon(icon, null, tint = DoctorTeal); Spacer(Modifier.width(10.dp)); Text(announcement.message, Modifier.weight(1f), color = DoctorMuted) }
        Row(verticalAlignment = Alignment.CenterVertically) { StatusPill(if (announcement.active) "Visible to patients" else "Hidden", announcement.active); if (onToggle != null) { Spacer(Modifier.weight(1f)); Switch(announcement.active, { onToggle() }) } }
    }
}

@Composable fun AssistantsScreen(state: DoctorUiState, onBack: () -> Unit, onTogglePermission: (String, Permission) -> Unit) {
    LazyColumn(page.padding(20.dp), verticalArrangement = Arrangement.spacedBy(15.dp)) {
        item { PageHeader("Assistants", onBack) }
        item { ElevatedSection("Staff access", "Each assistant uses individual credentials and backend-enforced permissions.") { PrimaryAction("Add assistant", {}, icon = Icons.Outlined.PersonAdd) } }
        items(state.assistants, key = { it.id }) { assistant ->
            ElevatedSection(assistant.name, assistant.phone) {
                StatusPill(if (assistant.active) "Active" else "Disabled", assistant.active)
                Permission.entries.forEach { permission -> Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Text(permission.name.replace("_", " ").lowercase().replaceFirstChar(Char::uppercase), Modifier.weight(1f), fontSize = 12.sp); Switch(permission in assistant.permissions, { onTogglePermission(assistant.id, permission) }) } }
            }
        }
    }
}

@Composable fun ProfileScreen(state: DoctorUiState, onBack: () -> Unit, onHome: () -> Unit, onQueue: () -> Unit, onAppointments: () -> Unit) {
    Scaffold(containerColor = DoctorBackground, bottomBar = { DoctorBottomBar(DoctorBottomDestination.PROFILE, onHome, onQueue, onAppointments, {}) }) { padding ->
        LazyColumn(Modifier.padding(padding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(15.dp)) {
            item { PageHeader("Doctor profile", onBack) }
            item { ElevatedSection(state.profile.name, state.profile.specialty) { Row(verticalAlignment = Alignment.CenterVertically) { StatusPill(if (state.profile.verified) "Verified profile" else "Verification pending", state.profile.verified); Spacer(Modifier.weight(1f)); Icon(Icons.Outlined.Verified, null, tint = DoctorTeal) }; DetailLine(Icons.Outlined.School, state.profile.qualification); DetailLine(Icons.Outlined.Badge, state.profile.registrationNumber); DetailLine(Icons.Outlined.WorkHistory, "${state.profile.experienceYears} years experience"); DetailLine(Icons.Outlined.Payments, "₹${state.profile.consultationFee} consultation fee") } }
            item { ElevatedSection("About") { Text(state.profile.about, color = DoctorMuted); PrimaryAction("Edit profile information", {}, icon = Icons.Outlined.Edit) } }
            item { ElevatedSection("Verification rule") { Text("Changes to registration number, specialty or credentials will require Admin review.", color = DoctorMuted) } }
        }
    }
}

@Composable private fun DetailLine(icon: ImageVector, text: String) { Row(verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = DoctorTeal); Spacer(Modifier.width(11.dp)); Text(text, color = DoctorMuted) } }