package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF38BDF8),
    onPrimary = Color(0xFF00354E),
    primaryContainer = Color(0xFF004D70),
    onPrimaryContainer = Color(0xFFCBE6FF),
    secondary = Color(0xFF2DD4BF),
    onSecondary = Color(0xFF003731),
    background = MedDarkBg,
    surface = MedDarkSurface,
    surfaceVariant = MedDarkSurfaceVariant,
    onBackground = MedDarkTextPrimary,
    onSurface = MedDarkTextPrimary,
    onSurfaceVariant = MedDarkTextSecondary
)

private val LightColorScheme = lightColorScheme(
    primary = MedTealPrimary,
    onPrimary = Color.White,
    primaryContainer = MedTealLight,
    onPrimaryContainer = MedTealDark,
    secondary = MedSecondary,
    onSecondary = Color.White,
    secondaryContainer = MedSecondaryLight,
    background = MedBackground,
    surface = MedSurface,
    surfaceVariant = MedSurfaceVariant,
    onBackground = MedTextPrimary,
    onSurface = MedTextPrimary,
    onSurfaceVariant = MedTextSecondary
)

private val HighContrastColorScheme = darkColorScheme(
    primary = HighContrastPrimary,
    onPrimary = Color.Black,
    secondary = HighContrastAccent,
    onSecondary = Color.Black,
    background = HighContrastBg,
    surface = HighContrastSurface,
    onBackground = HighContrastText,
    onSurface = HighContrastText,
    outline = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    highContrast: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        highContrast -> HighContrastColorScheme
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
