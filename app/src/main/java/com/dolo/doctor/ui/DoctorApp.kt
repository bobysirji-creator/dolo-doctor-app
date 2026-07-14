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
import com.dolo.doctor.ui.screens.*

private object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val HOME = "home"
    const val QUEUE = "queue"
    const val APPOINTMENTS = "appointments"
    const val CLINIC = "clinic"
    const val AVAILABILITY = "availability"
    const val ANNOUNCEMENTS = "announcements"
    const val ASSISTANTS = "assistants"
    const val PROFILE = "profile"
}

@Composable fun DoloDoctorApp(
    authRepository: AuthRepository,
    doctorStateStore: DoctorStateStore,
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
    doctorViewModel: DoctorViewModel = viewModel(factory = DoctorViewModelFactory(doctorStateStore)),
    authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(authRepository))
) {
    val nav = rememberNavController()
    val state = doctorViewModel.uiState
    val authState = authViewModel.uiState
    val permissions = doctorViewModel.permissions()
    val startDestination = remember { if (authState.session == null) Routes.SPLASH else Routes.HOME }

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

    NavHost(navController = nav, startDestination = startDestination) {
        composable(Routes.SPLASH) {
            SplashScreen {
                nav.navigate(Routes.LOGIN) { popUpTo(Routes.SPLASH) { inclusive = true } }
            }
        }
        composable(Routes.LOGIN) {
            LaunchedEffect(authState.session) {
                if (authState.session != null) nav.navigate(Routes.HOME) { popUpTo(Routes.LOGIN) { inclusive = true } }
            }
            LoginScreen(authState, authViewModel::selectRole, authViewModel::updatePhone, authViewModel::updatePin, authViewModel::login)
        }
        composable(Routes.HOME) {
            DashboardScreen(state, permissions, darkTheme, onToggleTheme, ::queue, ::appointments, { protectedDoctorRoute(Routes.CLINIC) }, { protectedDoctorRoute(Routes.AVAILABILITY) }, { protectedDoctorRoute(Routes.ANNOUNCEMENTS) }, { protectedDoctorRoute(Routes.ASSISTANTS) }, ::profile, {
                authViewModel.logout(); doctorViewModel.logout(authRepository.removedAssistantIds()); nav.navigate(Routes.LOGIN) { popUpTo(Routes.HOME) { inclusive = true } }
            })
        }
        composable(Routes.QUEUE) { QueueScreen(state, permissions, nav::popBackStack, ::home, ::appointments, ::profile, doctorViewModel::toggleQueue, doctorViewModel::callNext, doctorViewModel::updateAppointment) }
        composable(Routes.APPOINTMENTS) { AppointmentsScreen(state, permissions, nav::popBackStack, ::home, ::queue, ::profile) }
        composable(Routes.CLINIC) { ClinicScreen(state, nav::popBackStack) }
        composable(Routes.AVAILABILITY) { AvailabilityScreen(state, nav::popBackStack, doctorViewModel::toggleAppointments) }
        composable(Routes.ANNOUNCEMENTS) { AnnouncementsScreen(state, nav::popBackStack, doctorViewModel::toggleAnnouncement) }
        composable(Routes.ASSISTANTS) { AssistantsScreen(state, nav::popBackStack, doctorViewModel::togglePermission) { assistantId -> if (doctorViewModel.deleteAssistant(assistantId)) authRepository.removeAssistant(assistantId) } }
        composable(Routes.PROFILE) { ProfileScreen(state, nav::popBackStack, ::home, ::queue, ::appointments) }
    }
}