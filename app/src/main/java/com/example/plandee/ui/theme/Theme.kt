package com.example.plandee.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = NeonEmeraldGlow,         // Electric Indigo
    secondary = NeonCyanGlow,          // Cyber Turquoise
    tertiary = RetroAmberGold,         // Sunrise Amber
    background = RetroTactileBg,       // Deep Obsidian
    surface = RetroCardSurface,
    onPrimary = SlateTextPrimary,
    onSecondary = SlateTextPrimary,
    onBackground = SlateTextPrimary,
    onSurface = SlateTextPrimary,
    onSurfaceVariant = SlateTextSecondary,
    outline = RetroBorderMetallic,
    surfaceContainer = RetroCardSurfaceElevated
)

private val LightColorScheme = lightColorScheme(
    primary = NeonEmeraldGlow,
    secondary = NeonCyanGlow,
    tertiary = RetroAmberGold,
    background = RetroTactileBg,
    surface = RetroCardSurface,
    onPrimary = SlateTextPrimary,
    onSecondary = SlateTextPrimary,
    onBackground = SlateTextPrimary,
    onSurface = SlateTextPrimary,
    onSurfaceVariant = SlateTextSecondary,
    outline = RetroBorderMetallic,
    surfaceContainer = RetroCardSurfaceElevated
)

@Composable
fun PlanDeeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = RetroTactileBg.toArgb()
            window.navigationBarColor = RetroTactileBg.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
