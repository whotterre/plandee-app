package com.example.plandee.ui.theme

import android.app.Activity
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.plandee.R

val PlanDeeDarkColorScheme = darkColorScheme(
    surface = RetroTactileBg,               // Deep Vintage Cyber Dark Navy
    primary = NeonEmeraldGlow,              // Neon Telecom Emerald Accent
    surfaceContainer = RetroCardSurface,   // Tactile 3D Container
    surfaceContainerHigh = RetroCardSurfaceElevated,
    onSurface = Color(0xFFFFFFFF),          // High Emphasis Text
    onSurfaceVariant = Color(0xFF94A3B8),   // Medium Emphasis Text
    outline = RetroBorderMetallic           // Tactile Metallic Borders
)

val HankenGrotesk = FontFamily(
    Font(R.font.hanken_grotest_variable_font, FontWeight.Normal),
)

val PlanDeeTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = HankenGrotesk,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp
    ),
    titleMedium = TextStyle(
        fontFamily = HankenGrotesk,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = HankenGrotesk,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    labelLarge = TextStyle(
        fontFamily = HankenGrotesk,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp
    )
)

val PlanDeeShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp)
)

@Composable
fun PlanDeeTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = PlanDeeDarkColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            window.navigationBarColor = colorScheme.surface.toArgb()
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = PlanDeeTypography,
        shapes = PlanDeeShapes,
        content = content
    )
}
