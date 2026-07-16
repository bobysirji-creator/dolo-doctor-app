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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dolo.doctor.auth.AuthUiState
import com.dolo.doctor.data.model.*
import com.dolo.doctor.ui.components.*
import com.dolo.doctor.printing.AndroidTokenReceiptPrinter
import kotlinx.coroutines.delay

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
            Text("New assistants use the mobile number and one-time PIN generated by the Doctor.", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
    onHistory: () -> Unit,
    onClinic: () -> Unit,
    onActivity: () -> Unit,
    onReports: () -> Unit,
    onSync: () -> Unit,
    onAvailability: () -> Unit,
    onAnnouncements: () -> Unit,
    onAssistants: () -> Unit,
    onProfile: () -> Unit,
    onNotifications: () -> Unit,
    onLogout: () -> Unit
) {
    val doctorMode = state.role == UserRole.DOCTOR
    val assistantName = state.assistants.firstOrNull { it.id == state.activeAssistantId }?.name ?: "Assistant"
    val canViewQueue = doctorMode || Permission.VIEW_QUEUE in permissions
    val canViewAppointments = doctorMode || Permission.VIEW_TODAY_APPOINTMENTS in permissions
    val canViewClinic = doctorMode || Permission.VIEW_CLINIC in permissions || Permission.MANAGE_CLINIC_AVAILABILITY in permissions
    val canViewReports = doctorMode || Permission.VIEW_REPORTS in permissions || Permission.VIEW_PATIENT_FEEDBACK in permissions || Permission.SEND_QUEUE_DELAY_NOTICE in permissions
    val morningQueue = state.sessionQueues.firstOrNull { it.session == "Morning" } ?: ConsultationQueue("Morning", state.queueState, state.currentToken)
    val eveningQueue = state.sessionQueues.firstOrNull { it.session == "Evening" } ?: ConsultationQueue("Evening", QueueState.NOT_STARTED, 0)
    var confirmLogout by remember { mutableStateOf(false) }
    val unreadNotifications = state.auditEvents.count { it.sequence > state.notificationReadThrough }
    Scaffold(containerColor = MaterialTheme.colorScheme.background, bottomBar = { DoctorBottomBar(DoctorBottomDestination.HOME, {}, onQueue, onAppointments, onProfile, profileEnabled = doctorMode) }) { padding ->
        LazyColumn(Modifier.padding(padding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                Column(Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth().heightIn(min = 40.dp), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.Top) {
                        IconButton(onToggleTheme) { Icon(if (darkTheme) Icons.Outlined.LightMode else Icons.Outlined.DarkMode, if (darkTheme) "Use light theme" else "Use dark theme") }
                        BadgedBox(badge = { if (unreadNotifications > 0) Badge { Text(unreadNotifications.coerceAtMost(99).toString()) } }) {
                            IconButton(onNotifications) { Icon(Icons.Outlined.Notifications, "Notifications") }
                        }
                        IconButton(onClick = { confirmLogout = true }) { Icon(Icons.Outlined.Logout, "Logout") }
                    }
                    DoctorBrand()
                    Text(if (doctorMode) state.profile.name else assistantName, Modifier.fillMaxWidth(), fontSize = 23.sp, lineHeight = 29.sp, fontWeight = FontWeight.ExtraBold)
                    Text(if (doctorMode) state.profile.specialty else "Assistant • ${permissions.size} permissions", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            item { Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { MetricTile("Morning token", morningQueue.currentToken.toString(), Modifier.weight(1f), MaterialTheme.colorScheme.error); MetricTile("Evening token", eveningQueue.currentToken.toString(), Modifier.weight(1f), MaterialTheme.colorScheme.tertiary) } }
            item { ElevatedSection("Today's queue", state.clinics.first().name + " • " + state.queueDate) { Column(verticalArrangement = Arrangement.spacedBy(6.dp)) { Row { Text("Morning", Modifier.weight(1f)); StatusPill(morningQueue.state.name.replace("_", " "), morningQueue.state == QueueState.ACTIVE) }; Row { Text("Evening", Modifier.weight(1f)); StatusPill(eveningQueue.state.name.replace("_", " "), eveningQueue.state == QueueState.ACTIVE) } }; PrimaryAction("Open live queue", onQueue, enabled = canViewQueue, icon = Icons.Outlined.FormatListNumbered) } }
            item { Text("Clinic tools", style = MaterialTheme.typography.titleLarge) }
            item { ToolRow(onAppointments, if (doctorMode) onHistory else onClinic, canViewAppointments, if (doctorMode) true else canViewClinic, secondLabel = if (doctorMode) "Queue history" else "Clinic", secondIcon = if (doctorMode) Icons.Outlined.History else Icons.Outlined.Business) }
            if (doctorMode) {
                item { ToolRow(onClinic, onReports, true, true, "Clinic", "Reports", Icons.Outlined.Business, Icons.Outlined.Insights) }
                item {
                    ElevatedSection("Shared Patient App integration", state.syncMessage) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            StatusPill(state.syncStatus.name.replace("_", " "), state.syncStatus == SyncStatus.SYNCED)
                            Spacer(Modifier.weight(1f))
                            Text("Revision " + state.syncRevision, fontWeight = FontWeight.Bold)
                        }
                        PrimaryAction("Open sync center", onSync, icon = Icons.Outlined.CloudSync)
                    }
                }
                item { ToolRow(onActivity, onAvailability, true, true, "Activity log", "Availability", Icons.Outlined.FactCheck, Icons.Outlined.EventBusy) }
                item { ToolRow(onAnnouncements, onAssistants, true, true, "Updates", "Assistants", Icons.Outlined.Campaign, Icons.Outlined.Groups) }
            } else {
                if (canViewReports || Permission.MANAGE_ANNOUNCEMENTS in permissions) {
                    item { ToolRow(onReports, onAnnouncements, canViewReports, Permission.MANAGE_ANNOUNCEMENTS in permissions, "Reports", "Updates", Icons.Outlined.Insights, Icons.Outlined.Campaign) }
                }
                item { ElevatedSection("Assistant access") { permissions.sortedBy { it.name }.forEach { Text("• ${it.name.replace("_", " ").lowercase().replaceFirstChar(Char::uppercase)}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp) } } }
            }
            if (doctorMode || Permission.MANAGE_ANNOUNCEMENTS in permissions) {
                item { Text("Active doctor updates", style = MaterialTheme.typography.titleLarge) }
                items(state.announcements.filter { it.active && state.queueDate >= it.startsOn && state.queueDate <= it.endsOn }.take(2), key = { it.id }) { AnnouncementCard(it, null) }
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

@Composable fun QueueScreen(
    state: DoctorUiState,
    permissions: Set<Permission>,
    onBack: () -> Unit,
    onHome: () -> Unit,
    onAppointments: () -> Unit,
    onProfile: () -> Unit,
    onSelectSession: (String) -> Unit,
    onToggleQueue: (String) -> Unit,
    onCallNext: (String) -> Unit,
    onUpdate: (String, AppointmentStatus) -> Unit,
    onResumeSkipped: (String) -> Boolean,
    onRejoin: (String) -> Boolean,
    onCloseSession: (String) -> Boolean
) {
    val doctorMode = state.role == UserRole.DOCTOR
    val canView = doctorMode || Permission.VIEW_QUEUE in permissions
    val canUpdate = doctorMode || Permission.UPDATE_QUEUE in permissions
    val canCallNext = doctorMode || Permission.CALL_NEXT_PATIENT in permissions
    val canMarkArrived = doctorMode || Permission.MARK_PATIENT_ARRIVED in permissions
    val canMarkAbsent = doctorMode || Permission.MARK_PATIENT_ABSENT in permissions
    val canMarkCompleted = doctorMode || Permission.MARK_PATIENT_COMPLETED in permissions
    val selectedSession = state.selectedSession
    val queue = state.sessionQueues.firstOrNull { it.session == selectedSession }
        ?: ConsultationQueue(selectedSession, if (selectedSession == "Morning") state.queueState else QueueState.NOT_STARTED, if (selectedSession == "Morning") state.currentToken else 0)
    val sessionAppointments = state.appointments.filter { it.session == selectedSession && it.paymentStatus != PaymentStatus.PENDING && it.receiptNumber.isNotBlank() }
    val hasNextPatient = sessionAppointments.any {
        it.availabilityImpactStatus in setOf(AvailabilityImpactStatus.NONE, AvailabilityImpactStatus.RESOLVED) &&
            it.status in setOf(AppointmentStatus.BOOKED, AppointmentStatus.ARRIVED, AppointmentStatus.WAITING)
    }
    val hasCurrentConsultation = sessionAppointments.any { it.token == queue.currentToken && it.status == AppointmentStatus.IN_CONSULTATION }
    val progressedOrder = sessionAppointments.filter { it.status in setOf(AppointmentStatus.IN_CONSULTATION, AppointmentStatus.COMPLETED, AppointmentStatus.SKIPPED) }.maxOfOrNull { it.queueOrder } ?: 0
    var confirmCloseSession by remember { mutableStateOf(false) }
    Scaffold(containerColor = MaterialTheme.colorScheme.background, bottomBar = { DoctorBottomBar(DoctorBottomDestination.QUEUE, onHome, {}, onAppointments, onProfile, profileEnabled = doctorMode) }) { padding ->
        LazyColumn(Modifier.padding(padding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item { PageHeader("Live queue", onBack) }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf("Morning", "Evening").forEach { session ->
                        val sessionQueue = state.sessionQueues.firstOrNull { it.session == session }
                        FilterChip(
                            selected = selectedSession == session,
                            onClick = { onSelectSession(session) },
                            label = { Text(session) },
                            leadingIcon = { Icon(if (session == "Morning") Icons.Outlined.LightMode else Icons.Outlined.DarkMode, null) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            if (!canView) item { ElevatedSection("Access restricted") { Text("This assistant account does not have VIEW_QUEUE permission.", color = MaterialTheme.colorScheme.error) } }
            else {
                item { Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { MetricTile(if (hasCurrentConsultation) "In consultation" else "Last token", queue.currentToken.toString(), Modifier.weight(1f), MaterialTheme.colorScheme.error); MetricTile("Remaining", sessionAppointments.count { it.status in setOf(AppointmentStatus.BOOKED, AppointmentStatus.ARRIVED, AppointmentStatus.WAITING) }.toString(), Modifier.weight(1f), MaterialTheme.colorScheme.tertiary) } }
                item {
                    ElevatedSection(selectedSession + " queue controls", "Independent status: ${queue.state.name.lowercase().replaceFirstChar(Char::uppercase)}") {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button({ onToggleQueue(selectedSession) }, Modifier.weight(1f), enabled = canUpdate && queue.state != QueueState.CLOSED, elevation = ButtonDefaults.buttonElevation(7.dp)) {
                                Text(when (queue.state) { QueueState.ACTIVE -> "Pause"; QueueState.NOT_STARTED -> "Start queue"; QueueState.PAUSED -> "Resume"; QueueState.CLOSED -> "Session closed" })
                            }
                            Button({ onCallNext(selectedSession) }, Modifier.weight(1f), enabled = queue.state == QueueState.ACTIVE && canCallNext && (hasNextPatient || hasCurrentConsultation), elevation = ButtonDefaults.buttonElevation(7.dp)) {
                                Text(if (hasNextPatient) "Call next" else "Complete consultation")
                            }
                        }
                        if (doctorMode) {
                            OutlinedButton(onClick = { confirmCloseSession = true }, modifier = Modifier.fillMaxWidth(), enabled = queue.state != QueueState.CLOSED) {
                                Icon(Icons.Outlined.EventAvailable, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Close " + selectedSession + " session")
                            }
                        }
                        if (!canUpdate || !canCallNext) Text("Some controls are disabled by assistant permissions.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    }
                }
                if (sessionAppointments.isEmpty()) {
                    item { ElevatedSection("No fee-confirmed " + selectedSession.lowercase() + " appointments for " + state.queueDate) { Text("This session queue is independent from the other consultation session.", color = MaterialTheme.colorScheme.onSurfaceVariant) } }
                }
                items(sessionAppointments.sortedBy { it.queueOrder }, key = { it.id }) { appointment ->
                    QueueAppointmentCard(
                        appointment,
                        appointment.status == AppointmentStatus.BOOKED && appointment.queueOrder < progressedOrder,
                        canMarkArrived && queue.state != QueueState.CLOSED,
                        canMarkAbsent && queue.state != QueueState.CLOSED,
                        canMarkCompleted && queue.state != QueueState.CLOSED,
                        canUpdate && queue.state != QueueState.CLOSED,
                        !hasCurrentConsultation,
                        onUpdate,
                        onResumeSkipped,
                        onRejoin
                    )
                }
            }
        }
    }
    if (confirmCloseSession) {
        AlertDialog(
            onDismissRequest = { confirmCloseSession = false },
            icon = { Icon(Icons.Outlined.EventAvailable, null) },
            title = { Text("Close " + selectedSession + " session?") },
            text = { Text("Only the " + selectedSession + " queue and its booking will close. The other session stays independent. Daily history is finalized after both sessions close.") },
            confirmButton = { TextButton(onClick = { if (onCloseSession(selectedSession)) confirmCloseSession = false }) { Text("Close session") } },
            dismissButton = { TextButton(onClick = { confirmCloseSession = false }) { Text("Keep session open") } }
        )
    }
}
@Composable private fun QueueAppointmentCard(
    appointment: Appointment,
    isLateArrival: Boolean,
    canMarkArrived: Boolean,
    canMarkAbsent: Boolean,
    canMarkCompleted: Boolean,
    canUpdate: Boolean,
    canResumeNow: Boolean,
    onUpdate: (String, AppointmentStatus) -> Unit,
    onResumeSkipped: (String) -> Boolean,
    onRejoin: (String) -> Boolean
) {
    val availabilityReady = appointment.availabilityImpactStatus in setOf(
        AvailabilityImpactStatus.NONE,
        AvailabilityImpactStatus.RESOLVED
    )
    ElevatedSection("Token ${appointment.token} • ${appointment.patientName}", "${appointment.patientType} • ${appointment.session} • booked ${appointment.bookedAt} • INR ${appointment.consultationFee} ${appointment.paymentStatus.name}") {
        Row(verticalAlignment = Alignment.CenterVertically) {
            StatusPill(appointment.status.name.replace("_", " "), appointment.status !in setOf(AppointmentStatus.ABSENT, AppointmentStatus.SKIPPED))
            Spacer(Modifier.weight(1f))
            if (appointment.status == AppointmentStatus.BOOKED && canMarkArrived && availabilityReady) {
                TextButton({ onUpdate(appointment.id, AppointmentStatus.ARRIVED) }) { Text(if (isLateArrival) "Late arrival • rejoin" else "Mark arrived") }
            }
        }
        if (!availabilityReady) {
            StatusPill(
                "Availability follow-up: " + appointment.availabilityImpactStatus.name.replace("_", " "),
                false
            )
            Text(
                "Resolve this patient from Availability before continuing the consultation queue.",
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp
            )
        } else when (appointment.status) {
            AppointmentStatus.ARRIVED -> if (canMarkAbsent || canUpdate) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (canMarkAbsent) OutlinedButton({ onUpdate(appointment.id, AppointmentStatus.ABSENT) }, Modifier.weight(1f)) { Text("Absent") }
                    if (canUpdate) OutlinedButton({ onUpdate(appointment.id, AppointmentStatus.WAITING) }, Modifier.weight(1f)) { Text("Waiting") }
                }
            }
            AppointmentStatus.WAITING -> if (canMarkAbsent || canUpdate) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (canMarkAbsent) OutlinedButton({ onUpdate(appointment.id, AppointmentStatus.ABSENT) }, Modifier.weight(1f)) { Text("Absent") }
                    if (canUpdate) OutlinedButton({ onUpdate(appointment.id, AppointmentStatus.SKIPPED) }, Modifier.weight(1f)) { Text("Skip") }
                }
            }
            AppointmentStatus.SKIPPED -> if (canMarkAbsent || canUpdate) {
                if (canUpdate && canResumeNow) Button({ onResumeSkipped(appointment.id) }, Modifier.fillMaxWidth()) { Text("Resume consultation now") }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (canMarkAbsent) OutlinedButton({ onUpdate(appointment.id, AppointmentStatus.ABSENT) }, Modifier.weight(1f)) { Text("Absent") }
                    if (canUpdate) Button({ onRejoin(appointment.id) }, Modifier.weight(1f)) { Text("Rejoin at end") }
                }
            }
            AppointmentStatus.IN_CONSULTATION -> if (canMarkCompleted || canUpdate) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (canUpdate) OutlinedButton({ onUpdate(appointment.id, AppointmentStatus.SKIPPED) }, Modifier.weight(1f)) { Text("Skip") }
                    if (canMarkCompleted) Button({ onUpdate(appointment.id, AppointmentStatus.COMPLETED) }, Modifier.weight(1f)) { Text("Complete") }
                }
            }
            else -> Unit
        }
    }
}
@Composable fun AppointmentsScreen(
    state: DoctorUiState,
    permissions: Set<Permission>,
    onBack: () -> Unit,
    onHome: () -> Unit,
    onQueue: () -> Unit,
    onProfile: () -> Unit,
    onBookWalkIn: (WalkInBookingRequest) -> WalkInBookingResult,
    onReceipt: (String) -> TokenReceipt?,
    onConfirmFee: (String, Int, PaymentMethod) -> FeeConfirmationResult,
    isSessionBookingOpen: (String) -> Boolean,
    onSelectSession: (String) -> Unit,
    onRefreshDate: () -> Unit
) {
    var sessionClockTick by remember { mutableIntStateOf(0) }
    LaunchedEffect(state.queueDate) { onRefreshDate() }
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000L)
            onRefreshDate()
            sessionClockTick++
        }
    }
    val doctorMode = state.role == UserRole.DOCTOR
    val canView = doctorMode || Permission.VIEW_TODAY_APPOINTMENTS in permissions
    val canBookWalkIn = doctorMode || (Permission.BOOK_WALK_IN_APPOINTMENT in permissions && Permission.CONFIRM_CONSULTATION_FEE in permissions)
    val canGenerateReceipt = doctorMode || Permission.GENERATE_TOKEN_RECEIPT in permissions
    val canConfirmFee = doctorMode || Permission.CONFIRM_CONSULTATION_FEE in permissions
    val morningBookingOpen = sessionClockTick.let { isSessionBookingOpen("Morning") }
    val eveningBookingOpen = sessionClockTick.let { isSessionBookingOpen("Evening") }
    val selectedSession = state.selectedSession
    val maxTokens = state.clinics.firstOrNull()?.maxTokensPerSession ?: 0
    val morningCount = state.appointments.count { it.session == "Morning" }
    val eveningCount = state.appointments.count { it.session == "Evening" }
    var showWalkInBooking by remember { mutableStateOf(false) }
    var activeReceipt by remember { mutableStateOf<TokenReceipt?>(null) }
    var activeFeeAppointment by remember { mutableStateOf<Appointment?>(null) }
    Scaffold(containerColor = MaterialTheme.colorScheme.background, bottomBar = { DoctorBottomBar(DoctorBottomDestination.APPOINTMENTS, onHome, onQueue, {}, onProfile, profileEnabled = doctorMode) }) { padding ->
        LazyColumn(Modifier.padding(padding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item { PageHeader("Today's appointments", onBack) }
            if (!canView) item { ElevatedSection("Access restricted") { Text("This assistant account does not have VIEW_TODAY_APPOINTMENTS permission.", color = MaterialTheme.colorScheme.error) } }
            else {
                item {
                    ElevatedSection("Appointments and fee desk", "Only fee-confirmed appointments with a generated receipt are admitted to the selected session queue.") {
                        Text("Morning: " + (if (morningBookingOpen) "booking open" else "booking closed") + " • Evening: " + (if (eveningBookingOpen) "booking open" else "booking closed"), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Morning capacity: " + morningCount + "/" + maxTokens + (if (morningCount >= maxTokens) " - LIMIT REACHED" else ""))
                        Text("Evening capacity: " + eveningCount + "/" + maxTokens + (if (eveningCount >= maxTokens) " - LIMIT REACHED" else ""))
                        Text("The configured maximum includes Patient App and clinic walk-in appointments.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        Text("A session remains bookable before its end time; it does not wait for the start time.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        if (canBookWalkIn) PrimaryAction("Book walk-in patient", { showWalkInBooking = true }, enabled = morningBookingOpen || eveningBookingOpen, icon = Icons.Outlined.PersonAdd)
                    }
                }
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf("Morning", "Evening").forEach { session ->
                            FilterChip(
                                selected = selectedSession == session,
                                onClick = { onSelectSession(session) },
                                label = { Text(session + " (" + state.appointments.count { it.session == session } + ")") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                items(state.appointments.filter { it.session == selectedSession }.sortedBy { it.token }, key = { it.id }) { appointment ->
                    val feeConfirmed = appointment.paymentStatus != PaymentStatus.PENDING && appointment.receiptNumber.isNotBlank()
                    ElevatedSection(
                        "Token ${appointment.token} • ${appointment.patientName}",
                        appointment.patientType + " • " + appointment.session + " • " + appointment.bookingSource.name.replace("_", " ").lowercase()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            StatusPill(if (feeConfirmed) appointment.paymentStatus.name.replace("_", " ") else "FEE PENDING", feeConfirmed)
                            Spacer(Modifier.width(8.dp))
                            Text("INR " + (if (feeConfirmed) appointment.consultationFee else state.profile.consultationFee), fontWeight = FontWeight.Bold)
                            Spacer(Modifier.weight(1f))
                            if (feeConfirmed && canGenerateReceipt) TextButton({ activeReceipt = onReceipt(appointment.id) }) {
                                Icon(Icons.Outlined.ReceiptLong, null)
                                Spacer(Modifier.width(5.dp))
                                Text("Receipt")
                            }
                        }
                        if (feeConfirmed) {
                            Text("Queue admitted • " + appointment.status.name.replace("_", " ").lowercase() + " • " + (appointment.paymentMethod?.name ?: "PAYMENT"), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        } else {
                            Text("Not visible in Queue until the consultation fee is confirmed and receipt is generated.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                            if (canConfirmFee && canGenerateReceipt) Button({ activeFeeAppointment = appointment }, Modifier.fillMaxWidth()) {
                                Icon(Icons.Outlined.Payments, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Confirm fee & admit to queue")
                            }
                        }
                    }
                }
            }
        }
    }
    if (showWalkInBooking) {
        WalkInBookingDialog(
            morningOpen = morningBookingOpen,
            eveningOpen = eveningBookingOpen,
            consultationFee = state.profile.consultationFee,
            onDismiss = { showWalkInBooking = false },
            onBook = { request ->
                val result = onBookWalkIn(request)
                if (result.receipt != null) {
                    showWalkInBooking = false
                    activeReceipt = result.receipt
                }
                result.error
            }
        )
    }
    activeFeeAppointment?.let { appointment ->
        FeeConfirmationDialog(
            appointment = appointment,
            defaultFee = state.profile.consultationFee,
            onDismiss = { activeFeeAppointment = null },
            onConfirm = { amount, method ->
                val result = onConfirmFee(appointment.id, amount, method)
                if (result.receipt != null) {
                    activeFeeAppointment = null
                    activeReceipt = result.receipt
                }
                result.error
            }
        )
    }
    activeReceipt?.let { receipt -> TokenReceiptDialog(receipt, onDismiss = { activeReceipt = null }) }
}

@Composable private fun WalkInBookingDialog(
    morningOpen: Boolean,
    eveningOpen: Boolean,
    consultationFee: Int,
    onDismiss: () -> Unit,
    onBook: (WalkInBookingRequest) -> String?
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var patientType by remember { mutableStateOf("Self") }
    var session by remember { mutableStateOf(if (morningOpen) "Morning" else "Evening") }
    var feeText by remember { mutableStateOf(consultationFee.toString()) }
    var paymentMethod by remember { mutableStateOf(PaymentMethod.CASH) }
    var error by remember { mutableStateOf<String?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Outlined.PersonAdd, null) },
        title = { Text("Book clinic walk-in") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("The patient will be marked arrived, allotted the next token and shown a compulsory receipt.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedTextField(name, { name = it; error = null }, Modifier.fillMaxWidth(), label = { Text("Patient name") }, singleLine = true)
                OutlinedTextField(phone, { phone = it.filter(Char::isDigit).take(10); error = null }, Modifier.fillMaxWidth(), label = { Text("Mobile number") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), singleLine = true)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(patientType == "Self", { patientType = "Self" }, { Text("Self") })
                    FilterChip(patientType == "Family member", { patientType = "Family member" }, { Text("Family member") })
                }
                OutlinedTextField(
                    value = if (paymentMethod == PaymentMethod.WAIVED) "0" else feeText,
                    onValueChange = { feeText = it.filter(Char::isDigit).take(6); error = null },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Consultation fee (INR)") },
                    enabled = paymentMethod != PaymentMethod.WAIVED,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                Text("Payment received by", fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(PaymentMethod.CASH, PaymentMethod.UPI, PaymentMethod.CARD).forEach { method ->
                        FilterChip(paymentMethod == method, { paymentMethod = method }, { Text(method.name) })
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(PaymentMethod.ONLINE, PaymentMethod.WAIVED).forEach { method ->
                        FilterChip(paymentMethod == method, { paymentMethod = method }, { Text(method.name) })
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(session == "Morning", { session = "Morning" }, { Text(if (morningOpen) "Morning" else "Morning closed") }, enabled = morningOpen)
                    FilterChip(session == "Evening", { session = "Evening" }, { Text(if (eveningOpen) "Evening" else "Evening closed") }, enabled = eveningOpen)
                }
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            Button({ error = onBook(WalkInBookingRequest(name, phone, patientType, session, feeText.toIntOrNull() ?: 0, paymentMethod)) }, enabled = (session == "Morning" && morningOpen) || (session == "Evening" && eveningOpen)) {
                Text("Confirm fee, book & receipt")
            }
        },
        dismissButton = { TextButton(onDismiss) { Text("Cancel") } }
    )
}

@Composable private fun FeeConfirmationDialog(
    appointment: Appointment,
    defaultFee: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int, PaymentMethod) -> String?
) {
    var feeText by remember(appointment.id) { mutableStateOf(defaultFee.toString()) }
    var method by remember(appointment.id) { mutableStateOf(PaymentMethod.CASH) }
    var error by remember(appointment.id) { mutableStateOf<String?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Outlined.Payments, null) },
        title = { Text("Confirm consultation fee") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("${appointment.patientName} • ${appointment.session} token ${appointment.token}")
                Text("Receipt generation admits this patient to the ${appointment.session.lowercase()} queue.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedTextField(
                    value = if (method == PaymentMethod.WAIVED) "0" else feeText,
                    onValueChange = { feeText = it.filter(Char::isDigit).take(6); error = null },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Fee received (INR)") },
                    enabled = method != PaymentMethod.WAIVED,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(PaymentMethod.CASH, PaymentMethod.UPI, PaymentMethod.CARD).forEach { option ->
                        FilterChip(method == option, { method = option }, { Text(option.name) })
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(PaymentMethod.ONLINE, PaymentMethod.WAIVED).forEach { option ->
                        FilterChip(method == option, { method = option }, { Text(option.name) })
                    }
                }
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = { Button({ error = onConfirm(feeText.toIntOrNull() ?: 0, method) }) { Text("Confirm & generate receipt") } },
        dismissButton = { TextButton(onDismiss) { Text("Cancel") } }
    )
}
@Composable private fun TokenReceiptDialog(receipt: TokenReceipt, onDismiss: () -> Unit) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Outlined.ReceiptLong, null) },
        title = { Text("Token receipt") },
        text = {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Text(receipt.clinicName, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
                Text("TOKEN", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                Text(receipt.token.toString(), fontSize = 64.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                HorizontalDivider()
                Text(receipt.patientName, fontWeight = FontWeight.Bold)
                Text(receipt.doctorName)
                Text(receipt.appointmentDate + " • " + receipt.session)
                Text(if (receipt.paymentStatus == PaymentStatus.WAIVED) "Consultation fee: WAIVED" else "Fee paid: INR ${receipt.consultationFee} • ${receipt.paymentMethod.name}", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                if (receipt.paidAt.isNotBlank()) Text("Confirmed: " + receipt.paidAt, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                Text(receipt.receiptNumber, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Patient must hand this receipt to the doctor during consultation.", textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
            }
        },
        confirmButton = {
            Button({ AndroidTokenReceiptPrinter.print(context, receipt) }) {
                Icon(Icons.Outlined.Print, null)
                Spacer(Modifier.width(6.dp))
                Text("Print receipt")
            }
        },
        dismissButton = { TextButton(onDismiss) { Text("Close") } }
    )
}
@Composable fun QueueHistoryScreen(state: DoctorUiState, onBack: () -> Unit) {
    LazyColumn(page().padding(20.dp), verticalArrangement = Arrangement.spacedBy(15.dp)) {
        item { PageHeader("Queue history", onBack) }
        item {
            ElevatedSection("Daily archive", "Closed queues remain grouped by clinic date and preserve every patient's final status.") {
                Text(state.queueHistory.size.toString() + " archived day(s)", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (state.queueHistory.isEmpty()) {
            item { ElevatedSection("No archived queues") { Text("Use Close and archive day from Live queue, or let the app roll over automatically on the next date.", color = MaterialTheme.colorScheme.onSurfaceVariant) } }
        }
        items(state.queueHistory.sortedByDescending { it.date }, key = { it.date }) { history ->
            ElevatedSection(history.date, history.clinicName + " • " + history.closureReason + " at " + history.closedAt) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MetricTile("Final token", history.finalToken.toString(), Modifier.weight(1f), MaterialTheme.colorScheme.error)
                    MetricTile("Appointments", history.appointments.size.toString(), Modifier.weight(1f), MaterialTheme.colorScheme.tertiary)
                }
                val completed = history.appointments.count { it.status == AppointmentStatus.COMPLETED }
                val absent = history.appointments.count { it.status == AppointmentStatus.ABSENT }
                Text(completed.toString() + " completed • " + absent.toString() + " absent", color = MaterialTheme.colorScheme.onSurfaceVariant)
                history.appointments.sortedBy { it.token }.forEach { appointment ->
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Token " + appointment.token + " • " + appointment.patientName, fontWeight = FontWeight.Bold)
                            Text(appointment.patientType + " • " + appointment.session, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        }
                        StatusPill(appointment.status.name.replace("_", " "), appointment.status !in setOf(AppointmentStatus.ABSENT, AppointmentStatus.SKIPPED))
                    }
                }
            }
        }
    }
}
@Composable fun QueueActivityScreen(state: DoctorUiState, onBack: () -> Unit) {
    LazyColumn(page().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { PageHeader("Queue activity", onBack) }
        item {
            ElevatedSection("Audit trail", "Successful queue actions are recorded with actor, time and status context.") {
                Text(state.auditEvents.size.toString() + " recorded event(s)", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (state.auditEvents.isEmpty()) {
            item { ElevatedSection("No activity yet") { Text("Start or update the queue to create the first audit event.", color = MaterialTheme.colorScheme.onSurfaceVariant) } }
        }
        items(state.auditEvents.sortedByDescending { it.sequence }, key = { it.id }) { event ->
            ElevatedSection(
                event.action.name.replace("_", " ").lowercase().replaceFirstChar(Char::uppercase),
                event.date + " at " + event.time + " by " + event.actor
            ) {
                if (event.token != null) Text("Token " + event.token + (event.patientName?.let { " • " + it } ?: ""), fontWeight = FontWeight.Bold)
                if (event.fromStatus != null || event.toStatus != null) {
                    Text(
                        (event.fromStatus?.name?.replace("_", " ") ?: "-") + " → " + (event.toStatus?.name?.replace("_", " ") ?: "-"),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(event.detail, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
@Composable fun ClinicScreen(state: DoctorUiState, canEdit: Boolean, onBack: () -> Unit, onSaveClinic: (Clinic) -> String?) {
    var editingClinic by remember { mutableStateOf<Clinic?>(null) }
    LazyColumn(page().padding(20.dp), verticalArrangement = Arrangement.spacedBy(15.dp)) {
        item { PageHeader("Clinic & schedule", onBack) }
        item { ElevatedSection("Consultation setup", if (canEdit) "Update clinic contact details, sessions, capacity and consultation time." else "View the clinic schedule and consultation settings allowed by your Doctor.") { Text(if (canEdit) "Changes are stored locally and will later sync through the shared backend." else "Assistant access is read-only. Only the Doctor can edit clinic settings.", color = MaterialTheme.colorScheme.onSurfaceVariant) } }
        items(state.clinics, key = { it.id }) { clinic ->
            ElevatedSection(clinic.name, clinic.address) {
                DetailLine(Icons.Outlined.Phone, clinic.phone)
                DetailLine(Icons.Outlined.LightMode, "Morning: " + clinic.morningSession)
                DetailLine(Icons.Outlined.DarkMode, "Evening: " + clinic.eveningSession)
                DetailLine(Icons.Outlined.ConfirmationNumber, clinic.maxTokensPerSession.toString() + " tokens per session")
                DetailLine(Icons.Outlined.Schedule, "Average consultation: " + clinic.averageConsultationMinutes + " minutes")
                DetailLine(
                    Icons.Outlined.DateRange,
                    if (clinic.futureBookingEnabled) {
                        "Patient App future booking: Up to " + clinic.advanceBookingDays + " days"
                    } else {
                        "Patient App booking: Current day only"
                    }
                )
                DetailLine(Icons.Outlined.Storefront, "Clinic walk-ins: Current day only")
                if (canEdit) PrimaryAction("Edit clinic & schedule", { editingClinic = clinic }, icon = Icons.Outlined.CalendarMonth)
            }
        }
    }
    editingClinic?.let { clinic ->
        ClinicEditDialog(
            clinic = clinic,
            onDismiss = { editingClinic = null },
            onSave = onSaveClinic
        )
    }
}

@Composable private fun ClinicEditDialog(clinic: Clinic, onDismiss: () -> Unit, onSave: (Clinic) -> String?) {
    var name by remember(clinic.id) { mutableStateOf(clinic.name) }
    var address by remember(clinic.id) { mutableStateOf(clinic.address) }
    var phone by remember(clinic.id) { mutableStateOf(clinic.phone) }
    var morning by remember(clinic.id) { mutableStateOf(clinic.morningSession) }
    var evening by remember(clinic.id) { mutableStateOf(clinic.eveningSession) }
    var maxTokens by remember(clinic.id) { mutableStateOf(clinic.maxTokensPerSession.toString()) }
    var averageMinutes by remember(clinic.id) { mutableStateOf(clinic.averageConsultationMinutes.toString()) }
    var futureBookingEnabled by remember(clinic.id) { mutableStateOf(clinic.futureBookingEnabled) }
    var advanceBookingDays by remember(clinic.id) { mutableStateOf(clinic.advanceBookingDays.toString()) }
    var error by remember(clinic.id) { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit clinic & schedule") },
        text = {
            Column(Modifier.heightIn(max = 520.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Clinic name") }, singleLine = true)
                OutlinedTextField(address, { address = it }, label = { Text("Clinic address") }, minLines = 2)
                OutlinedTextField(phone, { phone = it }, label = { Text("Clinic phone") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
                OutlinedTextField(morning, { morning = it }, label = { Text("Morning session") }, supportingText = { Text("Example: 09:00 AM - 01:00 PM") }, singleLine = true)
                OutlinedTextField(evening, { evening = it }, label = { Text("Evening session") }, supportingText = { Text("Example: 05:00 PM - 09:00 PM") }, singleLine = true)
                OutlinedTextField(maxTokens, { maxTokens = it.filter(Char::isDigit) }, label = { Text("Maximum tokens per session") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(averageMinutes, { averageMinutes = it.filter(Char::isDigit) }, label = { Text("Average consultation minutes") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                HorizontalDivider()
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Allow Patient App future booking", fontWeight = FontWeight.Bold)
                        Text(
                            "Clinic walk-in booking always remains current-day only.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                    Switch(
                        checked = futureBookingEnabled,
                        onCheckedChange = { futureBookingEnabled = it; error = null }
                    )
                }
                if (futureBookingEnabled) {
                    OutlinedTextField(
                        advanceBookingDays,
                        { advanceBookingDays = it.filter(Char::isDigit).take(2); error = null },
                        label = { Text("Maximum advance booking days") },
                        supportingText = { Text("Choose 1 to 90 days") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
                error?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val result = onSave(
                    clinic.copy(
                        name = name,
                        address = address,
                        phone = phone,
                        morningSession = morning,
                        eveningSession = evening,
                        maxTokensPerSession = maxTokens.toIntOrNull() ?: -1,
                        averageConsultationMinutes = averageMinutes.toIntOrNull() ?: -1,
                        futureBookingEnabled = futureBookingEnabled,
                        advanceBookingDays = advanceBookingDays.toIntOrNull() ?: -1
                    )
                )
                error = result
                if (result == null) onDismiss()
            }) { Text("Save changes") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
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
    onCreateAssistant: (String, String, Set<Permission>) -> AssistantCreationResult,
    onSetActive: (String, Boolean) -> Boolean,
    onResetPin: (String) -> AssistantCredentialIssue?,
    onDeleteAssistant: (String) -> Unit
) {
    var pendingDeletion by remember { mutableStateOf<Assistant?>(null) }
    var showCreate by remember { mutableStateOf(false) }
    var issuedCredential by remember { mutableStateOf<AssistantCredentialIssue?>(null) }

    LazyColumn(page().padding(20.dp), verticalArrangement = Arrangement.spacedBy(15.dp)) {
        item { PageHeader("Assistants", onBack) }
        item {
            ElevatedSection(
                "Staff access",
                "Create individual credentials, disable access instantly and grant only the permissions each staff member needs."
            ) {
                PrimaryAction("Add assistant", { showCreate = true }, icon = Icons.Outlined.PersonAdd)
            }
        }
        items(state.assistants, key = { it.id }) { assistant ->
            ElevatedSection(assistant.name, assistant.phone) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    StatusPill(if (assistant.active) "Login enabled" else "Login disabled", assistant.active)
                    Spacer(Modifier.weight(1f))
                    Switch(assistant.active, { onSetActive(assistant.id, it) })
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { onResetPin(assistant.id)?.let { issuedCredential = it } },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Outlined.Password, null)
                        Spacer(Modifier.width(5.dp))
                        Text("Reset PIN")
                    }
                    TextButton(
                        onClick = { pendingDeletion = assistant },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Outlined.DeleteOutline, null)
                        Spacer(Modifier.width(5.dp))
                        Text("Delete")
                    }
                }
                HorizontalDivider()
                Text("Permissions", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Permission.entries.forEach { permission ->
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(permissionLabel(permission), Modifier.weight(1f), fontSize = 12.sp)
                        Switch(permission in assistant.permissions, { onTogglePermission(assistant.id, permission) })
                    }
                }
            }
        }
        if (state.assistants.isEmpty()) {
            item { ElevatedSection("No assistants") { Text("Add an assistant when clinic staff access is needed.", color = MaterialTheme.colorScheme.onSurfaceVariant) } }
        }
        item {
            ElevatedSection("Security note") {
                Text(
                    "Temporary PINs are displayed only when created or reset. The app stores a salted hash, not the readable PIN. Production authorization will be enforced by the shared backend.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (showCreate) {
        AssistantCreateDialog(
            onDismiss = { showCreate = false },
            onCreate = { name, phone, permissions ->
                val result = onCreateAssistant(name, phone, permissions)
                result.credential?.let {
                    issuedCredential = it
                    showCreate = false
                }
                result.error
            }
        )
    }

    issuedCredential?.let { credential ->
        AlertDialog(
            onDismissRequest = {},
            icon = { Icon(Icons.Outlined.Key, null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Assistant credentials") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(credential.assistant.name, fontWeight = FontWeight.Bold)
                    Text("Mobile: ${credential.assistant.phone}")
                    Text("Temporary PIN: ${credential.temporaryPin}", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Text("Record or share this PIN securely now. It will not be shown again.", color = MaterialTheme.colorScheme.error)
                }
            },
            confirmButton = { Button(onClick = { issuedCredential = null }) { Text("I saved it") } }
        )
    }

    pendingDeletion?.let { assistant ->
        AlertDialog(
            onDismissRequest = { pendingDeletion = null },
            icon = { Icon(Icons.Outlined.PersonRemove, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Delete ${assistant.name}?") },
            text = { Text("This permanently removes the local assistant profile, permissions and login credentials. Disable the account instead when access may be needed later.") },
            confirmButton = {
                TextButton(onClick = { onDeleteAssistant(assistant.id); pendingDeletion = null }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text("Delete assistant") }
            },
            dismissButton = { TextButton(onClick = { pendingDeletion = null }) { Text("Keep assistant") } }
        )
    }
}

@Composable private fun AssistantCreateDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String, Set<Permission>) -> String?
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var permissions by remember { mutableStateOf(setOf(Permission.VIEW_QUEUE, Permission.VIEW_TODAY_APPOINTMENTS)) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Outlined.PersonAdd, null, tint = MaterialTheme.colorScheme.primary) },
        title = { Text("Add assistant") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(name, { name = it; error = null }, label = { Text("Full name") }, singleLine = true)
                OutlinedTextField(
                    phone,
                    { phone = it.filter(Char::isDigit).take(10); error = null },
                    label = { Text("10-digit mobile number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true
                )
                Text("Initial permissions", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Permission.entries.forEach { permission ->
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(permissionLabel(permission), Modifier.weight(1f), fontSize = 12.sp)
                        Checkbox(
                            checked = permission in permissions,
                            onCheckedChange = {
                                permissions = if (it) permissions + permission else permissions - permission
                            }
                        )
                    }
                }
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            Button(onClick = { error = onCreate(name, phone, permissions) }) { Text("Create credentials") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

private fun permissionLabel(permission: Permission): String =
    permission.name.replace("_", " ").lowercase().replaceFirstChar(Char::uppercase)

@Composable fun ProfileScreen(state: DoctorUiState, onBack: () -> Unit, onHome: () -> Unit, onQueue: () -> Unit, onAppointments: () -> Unit, onSaveProfile: (DoctorProfile) -> String?) {
    var editingProfile by remember { mutableStateOf(false) }
    val pendingReview = state.profile.reviewStatus == ProfileReviewStatus.PENDING_REVIEW
    Scaffold(containerColor = MaterialTheme.colorScheme.background, bottomBar = { DoctorBottomBar(DoctorBottomDestination.PROFILE, onHome, onQueue, onAppointments, {}) }) { padding ->
        LazyColumn(Modifier.padding(padding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(15.dp)) {
            item { PageHeader("Doctor profile", onBack) }
            item {
                ElevatedSection(state.profile.name, state.profile.specialty) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        StatusPill(
                            if (pendingReview) "Sensitive changes pending Admin review" else if (state.profile.verified) "Verified profile" else "Verification pending",
                            state.profile.verified && !pendingReview
                        )
                        Spacer(Modifier.weight(1f))
                        Icon(Icons.Outlined.Verified, null, tint = MaterialTheme.colorScheme.primary)
                    }
                    DetailLine(Icons.Outlined.School, state.profile.qualification)
                    DetailLine(Icons.Outlined.Badge, state.profile.registrationNumber)
                    DetailLine(Icons.Outlined.WorkHistory, state.profile.experienceYears.toString() + " years experience")
                    DetailLine(Icons.Outlined.Payments, "₹" + state.profile.consultationFee + " consultation fee")
                }
            }
            item {
                ElevatedSection("About") {
                    Text(state.profile.about, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    PrimaryAction("Edit profile information", { editingProfile = true }, icon = Icons.Outlined.Edit)
                }
            }
            item { ElevatedSection("Verification rule") { Text("Name, fee, experience and About update immediately. Changes to registration number, specialty or qualifications are marked for Admin review.", color = MaterialTheme.colorScheme.onSurfaceVariant) } }
        }
    }
    if (editingProfile) {
        ProfileEditDialog(
            profile = state.profile,
            onDismiss = { editingProfile = false },
            onSave = onSaveProfile
        )
    }
}

@Composable private fun ProfileEditDialog(profile: DoctorProfile, onDismiss: () -> Unit, onSave: (DoctorProfile) -> String?) {
    var name by remember(profile) { mutableStateOf(profile.name) }
    var specialty by remember(profile) { mutableStateOf(profile.specialty) }
    var qualification by remember(profile) { mutableStateOf(profile.qualification) }
    var registration by remember(profile) { mutableStateOf(profile.registrationNumber) }
    var experience by remember(profile) { mutableStateOf(profile.experienceYears.toString()) }
    var fee by remember(profile) { mutableStateOf(profile.consultationFee.toString()) }
    var about by remember(profile) { mutableStateOf(profile.about) }
    var error by remember(profile) { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit doctor profile") },
        text = {
            Column(Modifier.heightIn(max = 520.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Doctor name") }, singleLine = true)
                OutlinedTextField(specialty, { specialty = it }, label = { Text("Specialty") }, supportingText = { Text("Changes require Admin review") }, singleLine = true)
                OutlinedTextField(qualification, { qualification = it }, label = { Text("Qualifications") }, supportingText = { Text("Changes require Admin review") }, singleLine = true)
                OutlinedTextField(registration, { registration = it }, label = { Text("Registration number") }, supportingText = { Text("Changes require Admin review") }, singleLine = true)
                OutlinedTextField(experience, { experience = it.filter(Char::isDigit) }, label = { Text("Experience in years") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(fee, { fee = it.filter(Char::isDigit) }, label = { Text("Consultation fee") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(about, { about = it }, label = { Text("About") }, minLines = 3, supportingText = { Text(about.length.toString() + "/500") })
                error?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val result = onSave(
                    profile.copy(
                        name = name,
                        specialty = specialty,
                        qualification = qualification,
                        registrationNumber = registration,
                        experienceYears = experience.toIntOrNull() ?: -1,
                        consultationFee = fee.toIntOrNull() ?: -1,
                        about = about
                    )
                )
                error = result
                if (result == null) onDismiss()
            }) { Text("Save profile") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
@Composable private fun DetailLine(icon: ImageVector, text: String) { Row(verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = MaterialTheme.colorScheme.primary); Spacer(Modifier.width(11.dp)); Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
