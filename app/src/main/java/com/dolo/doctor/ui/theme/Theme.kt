package com.dolo.doctor.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val DoctorNavy = Color(0xFF102A43)
val DoctorTeal = Color(0xFF058F86)
val DoctorMint = Color(0xFF24BEB2)
val DoctorBlue = Color(0xFF3676D8)
val DoctorCoral = Color(0xFFE65B70)
val DoctorBackground = Color(0xFFF4FAF9)
val DoctorSurface = Color(0xFFFFFFFF)
val DoctorSurfaceAlt = Color(0xFFE5F7F5)
val DoctorMuted = Color(0xFF66758A)
val DoctorBorder = Color(0xFFDCE8EA)

private val colors = lightColorScheme(
    primary = DoctorTeal,
    secondary = DoctorMint,
    tertiary = DoctorBlue,
    background = DoctorBackground,
    surface = DoctorSurface,
    surfaceVariant = DoctorSurfaceAlt,
    onPrimary = Color.White,
    onBackground = DoctorNavy,
    onSurface = DoctorNavy,
    outline = DoctorBorder,
    error = DoctorCoral
)

private val typography = Typography(
    headlineLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.ExtraBold, fontSize = 30.sp, color = DoctorNavy),
    headlineMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 25.sp, color = DoctorNavy),
    titleLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 21.sp, color = DoctorNavy),
    titleMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, color = DoctorNavy),
    bodyLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 16.sp, color = DoctorNavy),
    bodyMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 14.sp, color = DoctorMuted),
    labelLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 15.sp)
)

@Composable fun DoloDoctorTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = colors, typography = typography, content = content)
}