package com.buddingintents.letsgodutch.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors: ColorScheme = lightColorScheme(
    primary = MintGreen,
    onPrimary = Color.White,
    primaryContainer = MintFoam,
    onPrimaryContainer = MintTealDark,
    secondary = MintTeal,
    onSecondary = Color.White,
    secondaryContainer = MintFoamDeep,
    onSecondaryContainer = Charcoal,
    tertiary = AmberWarn,
    onTertiary = Charcoal,
    background = FogGrey,
    onBackground = Charcoal,
    surface = SurfaceLight,
    onSurface = Charcoal,
    surfaceVariant = SurfaceLightVariant,
    onSurfaceVariant = Slate,
    outline = OutlineLight,
    error = CoralError,
    onError = Color.White,
    errorContainer = CoralSoft,
    onErrorContainer = Color(0xFF5F1313),
)

private val DarkColors: ColorScheme = darkColorScheme(
    primary = MintGreen,
    onPrimary = Charcoal,
    primaryContainer = MintTealDark,
    onPrimaryContainer = MintFoam,
    secondary = MintTeal,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF0D5B50),
    onSecondaryContainer = TextOnDark,
    tertiary = AmberWarn,
    onTertiary = Charcoal,
    background = Night,
    onBackground = TextOnDark,
    surface = SurfaceDark,
    onSurface = TextOnDark,
    surfaceVariant = SurfaceDarkVariant,
    onSurfaceVariant = Color(0xFFB7C5D3),
    outline = OutlineDark,
    error = CoralError,
    onError = Color.White,
    errorContainer = Color(0xFF5D2525),
    onErrorContainer = Color(0xFFFFDAD6),
)

@Composable
fun LetsGoDutchTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = LetsGoDutchTypography,
        shapes = LetsGoDutchShapes,
        content = content,
    )
}
