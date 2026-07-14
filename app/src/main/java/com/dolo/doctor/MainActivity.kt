package com.dolo.doctor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.dolo.doctor.auth.LocalAuthRepository
import com.dolo.doctor.ui.DoloDoctorApp
import com.dolo.doctor.ui.theme.DoloDoctorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val authRepository = LocalAuthRepository(getSharedPreferences("dolo_doctor_auth", MODE_PRIVATE))
        setContent { DoloDoctorTheme { DoloDoctorApp(authRepository) } }
    }
}