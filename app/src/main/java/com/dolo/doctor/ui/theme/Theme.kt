package com.dolo.doctor.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Light mode mirrors the DO-LO Patient App palette.
val DoctorNavy = Color(0xFF0D2344)
val DoctorTeal = Color(0xFF08A89D)
val DoctorMint = Color(0xFF25C5B8)
val DoctorBlue = Color(0xFF1769D2)
val DoctorCoral = Color(0xFFD63C52)
val DoctorBackground = Color(0xFFF8FBFD)
val DoctorSurface = Color(0xFFFFFFFF)
val DoctorSurfaceAlt = Color(0xFFEAF9F8)
val DoctorMuted = Color(0xFF677086)
val DoctorBorder = Color(0xFFE5EBF2)

private val lightColors = lightColorScheme(
    primary = DoctorTeal,
    secondary = DoctorMint,
    tertiary = DoctorBlue,
    background = DoctorBackground,
    surface = DoctorSurface,
    surfaceVariant = DoctorSurfaceAlt,
    onPrimary = Color.White,
    onBackground = DoctorNavy,
    onSurface = DoctorNavy,
    onSurfaceVariant = DoctorMuted,
    outline = DoctorBorder,
    error = DoctorCoral,
    errorContainer = Color(0xFFFFE9ED)
)

private val darkColors = darkColorScheme(
    primary = Color(0xFF55D6CB),
    secondary = Color(0xFF7DE2DA),
    tertiary = Color(0xFF82B1FF),
    background = Color(0xFF0B1220),
    surface = Color(0xFF121C2B),
    surfaceVariant = Color(0xFF19313B),
    onPrimary = Color(0xFF05201E),
    onBackground = Color(0xFFE8EEF7),
    onSurface = Color(0xFFE8EEF7),
    onSurfaceVariant = Color(0xFFB8C5D6),
    outline = Color(0xFF3B4D63),
    error = Color(0xFFFF8293),
    errorContainer = Color(0xFF4B1F2A)
)

private val typography = Typography(
    headlineLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.ExtraBold, fontSize = 32.sp),
    headlineMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 26.sp),
    titleLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 22.sp),
    titleMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 17.sp),
    bodyLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 16.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 14.sp),
    labelLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 16.sp)
)

@Composable fun DoloDoctorTheme(darkTheme: Boolean = false, content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (darkTheme) darkColors else lightColors, typography = typography, content = content)
}