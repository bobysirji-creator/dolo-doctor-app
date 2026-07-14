package com.dolo.doctor.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dolo.doctor.data.DoctorViewModel
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

@Composable fun DoloDoctorApp(viewModel: DoctorViewModel = viewModel()) {
    val nav = rememberNavController()
    val state = viewModel.uiState
    fun home() = nav.navigate(Routes.HOME) { launchSingleTop = true }
    fun queue() = nav.navigate(Routes.QUEUE) { launchSingleTop = true }
    fun appointments() = nav.navigate(Routes.APPOINTMENTS) { launchSingleTop = true }
    fun profile() = nav.navigate(Routes.PROFILE) { launchSingleTop = true }

    NavHost(navController = nav, startDestination = Routes.SPLASH) {
        composable(Routes.SPLASH) { SplashScreen { nav.navigate(Routes.LOGIN) { popUpTo(Routes.SPLASH) { inclusive = true } } } }
        composable(Routes.LOGIN) { LoginScreen { role -> viewModel.login(role); nav.navigate(Routes.HOME) { popUpTo(Routes.LOGIN) { inclusive = true } } } }
        composable(Routes.HOME) { DashboardScreen(state, ::queue, ::appointments, { nav.navigate(Routes.CLINIC) }, { nav.navigate(Routes.AVAILABILITY) }, { nav.navigate(Routes.ANNOUNCEMENTS) }, { nav.navigate(Routes.ASSISTANTS) }, ::profile, { viewModel.logout(); nav.navigate(Routes.LOGIN) { popUpTo(Routes.HOME) { inclusive = true } } }) }
        composable(Routes.QUEUE) { QueueScreen(state, nav::popBackStack, ::home, ::appointments, ::profile, viewModel::toggleQueue, viewModel::callNext, viewModel::updateAppointment) }
        composable(Routes.APPOINTMENTS) { AppointmentsScreen(state, nav::popBackStack, ::home, ::queue, ::profile) }
        composable(Routes.CLINIC) { ClinicScreen(state, nav::popBackStack) }
        composable(Routes.AVAILABILITY) { AvailabilityScreen(state, nav::popBackStack, viewModel::toggleAppointments) }
        composable(Routes.ANNOUNCEMENTS) { AnnouncementsScreen(state, nav::popBackStack, viewModel::toggleAnnouncement) }
        composable(Routes.ASSISTANTS) { AssistantsScreen(state, nav::popBackStack, viewModel::togglePermission) }
        composable(Routes.PROFILE) { ProfileScreen(state, nav::popBackStack, ::home, ::queue, ::appointments) }
    }
}