package com.dolo.doctor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat
import com.dolo.doctor.auth.LocalAuthRepository
import com.dolo.doctor.auth.SessionFileStore
import com.dolo.doctor.settings.AppPreferences
import com.dolo.doctor.ui.DoloDoctorApp
import com.dolo.doctor.ui.theme.DoloDoctorTheme
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val settingsPreferences = getSharedPreferences("dolo_doctor_settings", MODE_PRIVATE)
        val authRepository = LocalAuthRepository(
            getSharedPreferences("dolo_doctor_auth", MODE_PRIVATE),
            SessionFileStore(File(filesDir, "doctor_session_v1")),
            settingsPreferences
        )
        val appPreferences = AppPreferences(settingsPreferences)

        setContent {
            var darkTheme by remember { mutableStateOf(appPreferences.isDarkTheme()) }
            DoloDoctorTheme(darkTheme = darkTheme) {
                val colorScheme = MaterialTheme.colorScheme
                SideEffect {
                    window.statusBarColor = colorScheme.background.toArgb()
                    window.navigationBarColor = colorScheme.surface.toArgb()
                    WindowCompat.getInsetsController(window, window.decorView).apply {
                        isAppearanceLightStatusBars = !darkTheme
                        isAppearanceLightNavigationBars = !darkTheme
                    }
                }
                DoloDoctorApp(
                    authRepository = authRepository,
                    darkTheme = darkTheme,
                    onToggleTheme = {
                        val updated = !darkTheme
                        if (appPreferences.setDarkTheme(updated)) darkTheme = updated
                    }
                )
            }
        }
    }
}