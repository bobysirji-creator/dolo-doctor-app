package com.dolo.doctor.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Shared DO-LO ecosystem palette, accepted first in the Patient App.
val DoctorBlue = Color(0xFF1D73E8)
val DoctorNavy = Color(0xFF061A3A)
val DoctorTeal = Color(0xFF008DBA)
val DoctorMint = Color(0xFF38C6E8)
val DoctorBackground = Color(0xFFF4F8FF)
val DoctorSurfaceAlt = Color(0xFFEAF2FF)
val DoctorMuted = Color(0xFF5A6B85)
val DoctorCoral = Color(0xFFE94F6D)

val LocalDoloDoctorDarkTheme = staticCompositionLocalOf { false }

private val lightColors = lightColorScheme(
    primary = DoctorBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCE9FF),
    onPrimaryContainer = Color(0xFF001A43),
    secondary = DoctorTeal,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD3F4FF),
    onSecondaryContainer = Color(0xFF002F3D),
    tertiary = DoctorCoral,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFE0E7),
    onTertiaryContainer = Color(0xFF5B1024),
    background = DoctorBackground,
    onBackground = DoctorNavy,
    surface = Color.White,
    onSurface = DoctorNavy,
    surfaceVariant = DoctorSurfaceAlt,
    onSurfaceVariant = DoctorMuted,
    outline = Color.Transparent,
    outlineVariant = Color.Transparent,
    error = Color(0xFFBA1A1A),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

private val darkColors = darkColorScheme(
    primary = Color(0xFF78A9FF),
    onPrimary = Color(0xFF001B3F),
    primaryContainer = Color(0xFF174584),
    onPrimaryContainer = Color(0xFFD8E7FF),
    secondary = Color(0xFF55D5F4),
    onSecondary = Color(0xFF003642),
    secondaryContainer = Color(0xFF074C5C),
    onSecondaryContainer = Color(0xFFC4F4FF),
    tertiary = Color(0xFFFFB0C1),
    onTertiary = Color(0xFF650027),
    tertiaryContainer = Color(0xFF84233E),
    onTertiaryContainer = Color(0xFFFFD9E1),
    background = Color(0xFF030817),
    onBackground = Color(0xFFE8F0FF),
    surface = Color(0xFF081428),
    onSurface = Color(0xFFE8F0FF),
    surfaceVariant = Color(0xFF10233E),
    onSurfaceVariant = Color(0xFFBAC8DE),
    outline = Color(0xFF526B91),
    outlineVariant = Color(0xFF203552),
    error = Color(0xFFFFB4AB),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

private val typography = Typography(
    displaySmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.ExtraBold, fontSize = 34.sp, lineHeight = 40.sp, letterSpacing = (-0.5).sp),
    headlineLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.ExtraBold, fontSize = 29.sp, lineHeight = 34.sp, letterSpacing = (-0.35).sp),
    headlineMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 29.sp, letterSpacing = (-0.2).sp),
    headlineSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 21.sp, lineHeight = 26.sp),
    titleLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 19.sp, lineHeight = 24.sp),
    titleMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 21.sp),
    titleSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 18.sp),
    bodyLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 16.sp, lineHeight = 23.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 12.sp, lineHeight = 17.sp),
    labelLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 15.sp, lineHeight = 19.sp),
    labelMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, lineHeight = 16.sp)
)

private val shapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(30.dp)
)

@Composable
fun DoloDoctorTheme(darkTheme: Boolean = false, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalDoloDoctorDarkTheme provides darkTheme) {
        MaterialTheme(
            colorScheme = if (darkTheme) darkColors else lightColors,
            typography = typography,
            shapes = shapes,
            content = content
        )
    }
}