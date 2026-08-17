package com.dolo.doctor.ui

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dolo.doctor.auth.AuthRepository
import com.dolo.doctor.auth.AuthViewModel
import com.dolo.doctor.auth.AuthViewModelFactory
import com.dolo.doctor.data.DoctorStateStore
import com.dolo.doctor.data.DoctorViewModel
import com.dolo.doctor.data.DoctorViewModelFactory
import com.dolo.doctor.data.model.UserRole
import com.dolo.doctor.data.model.Permission
import com.dolo.doctor.ui.navigation.DoctorMoreDestination
import com.dolo.doctor.data.model.AssistantCreationResult
import com.dolo.doctor.hosted.HostedStaffViewModel
import com.dolo.doctor.hosted.HostedStaffViewModelFactory
import com.dolo.doctor.hosted.HttpHostedStaffApi
import com.dolo.doctor.ui.screens.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val HOME = "home"
    const val QUEUE = "queue"
    const val APPOINTMENTS = "appointments"
    const val HISTORY = "history"
    const val ACTIVITY = "activity"
    const val REPORTS = "reports"
    const val SYNC = "sync"
    const val HOSTED_SYNC = "hosted-sync"
    const val BACKUP = "backup"
    const val CHANGE_PIN = "change-pin"
    const val CLINIC = "clinic"
    const val AVAILABILITY = "availability"
    const val ANNOUNCEMENTS = "announcements"
    const val ASSISTANTS = "assistants"
    const val PROFILE = "profile"
    const val NOTIFICATIONS = "notifications"
}

@Composable fun DoloDoctorApp(
    authRepository: AuthRepository,
    doctorStateStore: DoctorStateStore,
    hostedStaffApi: HttpHostedStaffApi,
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
    doctorViewModel: DoctorViewModel = viewModel(factory = DoctorViewModelFactory(doctorStateStore)),
    authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(authRepository, hostedStaffApi)),
    hostedViewModel: HostedStaffViewModel = viewModel(factory = HostedStaffViewModelFactory(hostedStaffApi))
) {
    val nav = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val activity = LocalActivity.current
    val state = doctorViewModel.uiState
    val authState = authViewModel.uiState
    val permissions = doctorViewModel.permissions()
    val startDestination = remember {
        when {
            authState.session == null -> Routes.SPLASH
            authState.session?.mustChangePin == true -> Routes.CHANGE_PIN
            else -> Routes.HOME
        }
    }

    LaunchedEffect(authState.session) {
        val session = authState.session
        if (session == null) doctorViewModel.logout(authRepository.removedAssistantIds())
        else {
            doctorViewModel.login(session.role, session.userId.takeIf { session.role == UserRole.ASSISTANT }, authRepository.removedAssistantIds())
            if (session.controlledPilot) hostedViewModel.refresh()
        }
    }

    LaunchedEffect(state.role) { state.role?.let(hostedViewModel::bindLocalRole) }
    val hostedSnapshot = hostedViewModel.uiState.snapshot?.takeIf { snapshot -> state.role?.let { com.dolo.doctor.hosted.HostedRoleBoundary.allows(it,snapshot.role) } == true }
    LaunchedEffect(hostedSnapshot?.role) { if(hostedSnapshot!=null) while(true){ delay(15_000);hostedViewModel.refresh() } }
    val hostedUnread = hostedSnapshot?.notifications?.count{!it.read} ?: 0
    fun home() = nav.navigate(Routes.HOME) {
        popUpTo(Routes.HOME) { inclusive = false }
        launchSingleTop = true
    }
    fun queue() = nav.navigate(Routes.QUEUE) { launchSingleTop = true }
    fun appointments() = nav.navigate(Routes.APPOINTMENTS) { launchSingleTop = true }
    fun more() { scope.launch { drawerState.open() } }
    fun profile() {
        if (state.role == UserRole.DOCTOR) nav.navigate(Routes.PROFILE) { launchSingleTop = true }
    }
    fun protectedDoctorRoute(route: String) {
        if (state.role == UserRole.DOCTOR) nav.navigate(route)
    }
    fun clinic() {
        if (doctorViewModel.canAccessClinic()) nav.navigate(Routes.CLINIC) { launchSingleTop = true }
    }
    fun reports() {
        if (doctorViewModel.canAccessReports()) nav.navigate(Routes.REPORTS) { launchSingleTop = true }
    }
    fun changePin() = nav.navigate(Routes.CHANGE_PIN) { launchSingleTop = true }
    fun backup() {
        if (state.role == UserRole.DOCTOR) nav.navigate(Routes.BACKUP) { launchSingleTop = true }
    }
    fun sync() {
        if (state.role == UserRole.DOCTOR) nav.navigate(Routes.SYNC) { launchSingleTop = true }
    }
    fun hostedSync() {
        if (state.role == UserRole.DOCTOR || state.activeAssistantId == "staff-1") nav.navigate(Routes.HOSTED_SYNC) { launchSingleTop = true }
    }

    fun announcements() {
        if (state.role == UserRole.DOCTOR || Permission.MANAGE_ANNOUNCEMENTS in permissions) nav.navigate(Routes.ANNOUNCEMENTS) { launchSingleTop = true }
    }
    fun logout() {
        hostedViewModel.localLogout()
        authViewModel.logout()
        doctorViewModel.logout(authRepository.removedAssistantIds())
        nav.navigate(Routes.LOGIN) {
            popUpTo(nav.graph.id) { inclusive = true }
            launchSingleTop = true
        }
    }
    fun openDrawerDestination(destination: DoctorMoreDestination) {
        scope.launch { drawerState.close() }
        when (destination) {
            DoctorMoreDestination.NOTIFICATIONS -> nav.navigate(Routes.NOTIFICATIONS) { launchSingleTop = true }
            DoctorMoreDestination.PROFILE -> profile()
            DoctorMoreDestination.CHANGE_PIN -> changePin()
            DoctorMoreDestination.AVAILABILITY -> protectedDoctorRoute(Routes.AVAILABILITY)
            DoctorMoreDestination.ANNOUNCEMENTS -> announcements()
            DoctorMoreDestination.ASSISTANTS -> protectedDoctorRoute(Routes.ASSISTANTS)
            DoctorMoreDestination.REPORTS -> reports()
            DoctorMoreDestination.HISTORY -> protectedDoctorRoute(Routes.HISTORY)
            DoctorMoreDestination.ACTIVITY -> protectedDoctorRoute(Routes.ACTIVITY)
            DoctorMoreDestination.HOSTED_SYNC -> hostedSync()
            DoctorMoreDestination.SYNC -> sync()
            DoctorMoreDestination.BACKUP -> backup()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            DoctorMoreDrawerContent(
                state = state,
                permissions = permissions,
                darkTheme = darkTheme,
                unreadNotifications = hostedUnread,
                onToggleTheme = onToggleTheme,
                onOpen = ::openDrawerDestination,
                onLogout = {
                    scope.launch { drawerState.close() }
                    logout()
                }
            )
        }
    ) {
        NavHost(navController = nav, startDestination = startDestination) {
        composable(Routes.SPLASH) {
            SplashScreen {
                nav.navigate(Routes.LOGIN) { popUpTo(Routes.SPLASH) { inclusive = true } }
            }
        }
        composable(Routes.LOGIN) {
            LaunchedEffect(authState.session) {
                authState.session?.let { session ->
                    nav.navigate(if (session.mustChangePin) Routes.CHANGE_PIN else Routes.HOME) { popUpTo(Routes.LOGIN) { inclusive = true } }
                }
            }
            LoginScreen(authState, authViewModel::selectLoginMode, authViewModel::selectRole, authViewModel::updatePhone, authViewModel::updatePin, authViewModel::selectPilotAction, authViewModel::updatePilotDoloId, authViewModel::updatePilotInviteCode, authViewModel::updatePilotCredential, authViewModel::login, authViewModel::loginPilot)
        }
        composable(Routes.HOME) {
            BackHandler(enabled = drawerState.isClosed) { activity?.finish() }
            LaunchedEffect(authState.session?.mustChangePin) {
                if (authState.session?.mustChangePin == true) nav.navigate(Routes.CHANGE_PIN) { popUpTo(Routes.HOME) { inclusive = true } }
            }
            val pilotSession = authState.session?.takeIf { it.controlledPilot }
            if (pilotSession != null) {
                PilotDoctorHomeScreen(
                    displayName = pilotSession.displayName,
                    doloId = pilotSession.userId,
                    hostedMessage = hostedViewModel.uiState.message,
                    loading = hostedViewModel.uiState.loading,
                    workspaceReady = hostedViewModel.uiState.snapshot != null,
                    onRefresh = hostedViewModel::refresh,
                    onHostedWorkspace = ::hostedSync,
                    onLogout = ::logout
                )
            } else {
                DashboardScreen(
                    state = state,
                    permissions = permissions,
                    hostedUnreadNotifications = hostedUnread,
                    onQueue = ::queue,
                    onAppointments = ::appointments,
                    onClinic = ::clinic,
                    onHostedSync = ::hostedSync,
                    onNotifications = { nav.navigate(Routes.NOTIFICATIONS) { launchSingleTop = true } },
                    onMore = ::more
                )
            }
        }
        composable(Routes.QUEUE) {
            QueueScreen(
                state,
                permissions,
                nav::popBackStack,
                ::home,
                ::appointments,
                ::clinic,
                doctorViewModel::selectSession,
                doctorViewModel::recurringSessionClosed,
                doctorViewModel::toggleQueue,
                doctorViewModel::callNext,
                doctorViewModel::updateAppointment,
                doctorViewModel::resumeSkippedConsultation,
                doctorViewModel::rejoinAppointment,
                doctorViewModel::closeSession
            )
        }
        composable(Routes.APPOINTMENTS) { AppointmentsScreen(state, permissions, nav::popBackStack, ::home, ::clinic, doctorViewModel::bookWalkIn, doctorViewModel::receiptFor, doctorViewModel::confirmConsultationFee, doctorViewModel::sessionBookingOpen, doctorViewModel::selectSession, doctorViewModel::refreshDate) }
        composable(Routes.HISTORY) { QueueHistoryScreen(state, nav::popBackStack) }
        composable(Routes.ACTIVITY) { QueueActivityScreen(state, nav::popBackStack) }
        composable(Routes.REPORTS) { ReportsScreen(state, permissions, doctorViewModel::operationalReport, nav::popBackStack, doctorViewModel::acknowledgeFeedback, doctorViewModel::sendQueueDelayNotice) }
        composable(Routes.HOSTED_SYNC) {
            val localRole = state.role ?: return@composable
            HostedStaffSyncScreen(localRole, nav::popBackStack, hostedViewModel)
        }
        composable(Routes.SYNC) { SyncCenterScreen(state, doctorViewModel.sharedBackendReadiness(), nav::popBackStack, doctorViewModel::publishLocalSnapshot, doctorViewModel::pullSharedSnapshot, doctorViewModel::simulatePatientAppBooking) }
        composable(Routes.BACKUP) { BackupScreen(nav::popBackStack, doctorViewModel::exportEncryptedBackup, doctorViewModel::restoreEncryptedBackup) }
        composable(Routes.CHANGE_PIN) {
            ChangePinScreen(
                required = authState.session?.mustChangePin == true,
                isDoctor = authState.session?.role == UserRole.DOCTOR,
                message = authState.pinChangeMessage,
                onBack = nav::popBackStack,
                onLogout = ::logout,
                onClearMessage = authViewModel::clearPinChangeMessage,
                onSubmit = { currentPin, newPin, confirmation ->
                    authViewModel.changePin(currentPin, newPin, confirmation).also { changed ->
                        if (changed) nav.navigate(Routes.HOME) { popUpTo(Routes.CHANGE_PIN) { inclusive = true } }
                    }
                }
            )
        }
        composable(Routes.CLINIC) { ClinicScreen(state, state.role == UserRole.DOCTOR, nav::popBackStack, ::home, ::appointments, doctorViewModel::updateClinic) }
        composable(Routes.AVAILABILITY) { AvailabilityManagementScreen(state, nav::popBackStack, doctorViewModel::saveAvailabilityBlock, doctorViewModel::setAvailabilityAppointmentsEnabled, doctorViewModel::deleteAvailabilityBlock, doctorViewModel::updateAffectedPatientStatus) }
        composable(Routes.ANNOUNCEMENTS) { AnnouncementManagementScreen(state, nav::popBackStack, doctorViewModel::saveAnnouncement, doctorViewModel::setAnnouncementActive, doctorViewModel::deleteAnnouncement) }
        composable(Routes.ASSISTANTS) {
            AssistantsScreen(
                state = state,
                onBack = nav::popBackStack,
                onTogglePermission = doctorViewModel::togglePermission,
                onCreateAssistant = { name, phone, assistantPermissions ->
                    val result = doctorViewModel.createAssistant(name, phone, assistantPermissions)
                    val credential = result.credential
                    if (credential != null && !authRepository.provisionAssistant(credential.assistant, credential.temporaryPin)) {
                        doctorViewModel.deleteAssistant(credential.assistant.id)
                        AssistantCreationResult(error = "Unable to save assistant credentials. Please try again.")
                    } else result
                },
                onSetActive = { assistantId, active ->
                    val changed = doctorViewModel.setAssistantActive(assistantId, active)
                    val assistant = doctorViewModel.uiState.assistants.firstOrNull { it.id == assistantId }
                    changed && assistant != null && authRepository.setAssistantActive(assistant)
                },
                onResetPin = { assistantId ->
                    doctorViewModel.resetAssistantPin(assistantId)?.takeIf { credential ->
                        authRepository.resetAssistantPin(credential.assistant, credential.temporaryPin)
                    }
                },
                onDeleteAssistant = { assistantId ->
                    if (doctorViewModel.deleteAssistant(assistantId)) authRepository.removeAssistant(assistantId)
                }
            )
        }
        composable(Routes.PROFILE) { ProfileScreen(state, nav::popBackStack, ::home, ::appointments, ::clinic, doctorViewModel::updateProfile) }
        composable(Routes.NOTIFICATIONS) { NotificationsScreen(hostedViewModel.uiState.copy(snapshot=hostedSnapshot), nav::popBackStack, hostedViewModel::markHostedNotificationsRead) }
    }
}
}
