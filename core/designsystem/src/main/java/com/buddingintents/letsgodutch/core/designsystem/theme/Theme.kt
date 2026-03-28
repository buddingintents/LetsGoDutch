package com.buddingintents.letsgodutch.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp

private val LightColors: ColorScheme = lightColorScheme(
    primary = Purple500,
    onPrimary = Color.White,
    primaryContainer = Purple100,
    onPrimaryContainer = Purple700,
    secondary = Teal500,
    onSecondary = Color.White,
    secondaryContainer = Teal50,
    onSecondaryContainer = Teal600,
    background = SurfaceLight,
    onBackground = DullBlack,
    surface = Color.White,
    onSurface = DullBlack,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight,
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    tertiary = Color(0xFF7D5260),
    onTertiary = Color.White,
)

private val DarkColors: ColorScheme = darkColorScheme(
    primary = Purple200,
    onPrimary = Purple700,
    primaryContainer = Purple700,
    onPrimaryContainer = Purple100,
    secondary = Teal200,
    onSecondary = Teal600,
    secondaryContainer = Teal600,
    onSecondaryContainer = Teal100,
    background = SurfaceDark,
    onBackground = TextOnDark,
    surface = SurfaceDarkVariant,
    onSurface = TextOnDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark,
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    tertiary = Color(0xFFEFB8C8),
    onTertiary = Color(0xFF492532),
)

private val BaseTypography = Typography()

private val CompactTypography = BaseTypography.copy(
    displayLarge = BaseTypography.displayLarge.copy(fontSize = 50.sp, lineHeight = 56.sp),
    displayMedium = BaseTypography.displayMedium.copy(fontSize = 42.sp, lineHeight = 48.sp),
    displaySmall = BaseTypography.displaySmall.copy(fontSize = 36.sp, lineHeight = 42.sp),
    headlineLarge = BaseTypography.headlineLarge.copy(fontSize = 30.sp, lineHeight = 36.sp),
    headlineMedium = BaseTypography.headlineMedium.copy(fontSize = 24.sp, lineHeight = 30.sp),
    headlineSmall = BaseTypography.headlineSmall.copy(fontSize = 20.sp, lineHeight = 26.sp),
    titleLarge = BaseTypography.titleLarge.copy(fontSize = 20.sp, lineHeight = 26.sp),
    titleMedium = BaseTypography.titleMedium.copy(fontSize = 16.sp, lineHeight = 22.sp),
    titleSmall = BaseTypography.titleSmall.copy(fontSize = 14.sp, lineHeight = 20.sp),
    bodyLarge = BaseTypography.bodyLarge.copy(fontSize = 14.sp, lineHeight = 20.sp),
    bodyMedium = BaseTypography.bodyMedium.copy(fontSize = 13.sp, lineHeight = 18.sp),
    bodySmall = BaseTypography.bodySmall.copy(fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = BaseTypography.labelLarge.copy(fontSize = 12.sp, lineHeight = 16.sp),
    labelMedium = BaseTypography.labelMedium.copy(fontSize = 11.sp, lineHeight = 14.sp),
    labelSmall = BaseTypography.labelSmall.copy(fontSize = 10.sp, lineHeight = 12.sp),
)

@Composable
fun LetsGoDutchTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = CompactTypography,
        content = content,
    )
}
