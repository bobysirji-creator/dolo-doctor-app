package com.dolo.doctor.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
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
import com.dolo.doctor.data.model.AssistantCreationResult
import com.dolo.doctor.hosted.HostedStaffViewModel
import com.dolo.doctor.hosted.HostedStaffViewModelFactory
import com.dolo.doctor.hosted.HttpHostedStaffApi
import com.dolo.doctor.ui.screens.*

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
    authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(authRepository)),
    hostedViewModel: HostedStaffViewModel = viewModel(factory = HostedStaffViewModelFactory(hostedStaffApi))
) {
    val nav = rememberNavController()
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
        else doctorViewModel.login(session.role, session.userId.takeIf { session.role == UserRole.ASSISTANT }, authRepository.removedAssistantIds())
    }

    fun home() = nav.navigate(Routes.HOME) { launchSingleTop = true }
    fun queue() = nav.navigate(Routes.QUEUE) { launchSingleTop = true }
    fun appointments() = nav.navigate(Routes.APPOINTMENTS) { launchSingleTop = true }
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
            LoginScreen(authState, authViewModel::selectRole, authViewModel::updatePhone, authViewModel::updatePin, authViewModel::login)
        }
        composable(Routes.HOME) {
            LaunchedEffect(authState.session?.mustChangePin) {
                if (authState.session?.mustChangePin == true) nav.navigate(Routes.CHANGE_PIN) { popUpTo(Routes.HOME) { inclusive = true } }
            }
            DashboardScreen(
                state,
                permissions,
                darkTheme,
                onToggleTheme,
                ::queue,
                ::appointments,
                { protectedDoctorRoute(Routes.HISTORY) },
                ::clinic,
                { protectedDoctorRoute(Routes.ACTIVITY) },
                ::reports,
                ::sync,
                ::hostedSync,
                ::backup,
                ::changePin,
                { protectedDoctorRoute(Routes.AVAILABILITY) },
                { protectedDoctorRoute(Routes.ANNOUNCEMENTS) },
                { protectedDoctorRoute(Routes.ASSISTANTS) },
                ::profile,
                { nav.navigate(Routes.NOTIFICATIONS) { launchSingleTop = true } },
                {
                    authViewModel.logout()
                    doctorViewModel.logout(authRepository.removedAssistantIds())
                    nav.navigate(Routes.LOGIN) { popUpTo(Routes.HOME) { inclusive = true } }
                }
            )
        }
        composable(Routes.QUEUE) {
            QueueScreen(
                state,
                permissions,
                nav::popBackStack,
                ::home,
                ::appointments,
                ::profile,
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
        composable(Routes.APPOINTMENTS) { AppointmentsScreen(state, permissions, nav::popBackStack, ::home, ::queue, ::profile, doctorViewModel::bookWalkIn, doctorViewModel::receiptFor, doctorViewModel::confirmConsultationFee, doctorViewModel::sessionBookingOpen, doctorViewModel::selectSession, doctorViewModel::refreshDate) }
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
                onLogout = {
                    authViewModel.logout()
                    doctorViewModel.logout(authRepository.removedAssistantIds())
                    nav.navigate(Routes.LOGIN) { popUpTo(Routes.CHANGE_PIN) { inclusive = true } }
                },
                onClearMessage = authViewModel::clearPinChangeMessage,
                onSubmit = { currentPin, newPin, confirmation ->
                    authViewModel.changePin(currentPin, newPin, confirmation).also { changed ->
                        if (changed) nav.navigate(Routes.HOME) { popUpTo(Routes.CHANGE_PIN) { inclusive = true } }
                    }
                }
            )
        }
        composable(Routes.CLINIC) { ClinicScreen(state, state.role == UserRole.DOCTOR, nav::popBackStack, doctorViewModel::updateClinic) }
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
        composable(Routes.PROFILE) { ProfileScreen(state, nav::popBackStack, ::home, ::queue, ::appointments, doctorViewModel::updateProfile) }
        composable(Routes.NOTIFICATIONS) { NotificationsScreen(state, nav::popBackStack, doctorViewModel::markNotificationRead, doctorViewModel::markAllNotificationsRead) }
    }
}
