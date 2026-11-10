package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val AlignerDarkColorScheme = darkColorScheme(
    primary = ElectricCyan,
    onPrimary = OnCyanPrimary,
    primaryContainer = CyanPrimaryContainer,
    onPrimaryContainer = ElectricCyan,
    secondary = SoftTechBlue,
    onSecondary = OnSoftTechBlue,
    secondaryContainer = TechBlueContainer,
    onSecondaryContainer = SoftTechBlue,
    tertiary = NeonEmerald,
    onTertiary = OnNeonEmerald,
    tertiaryContainer = EmeraldContainer,
    onTertiaryContainer = OnNeonEmerald,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceContainerHigh,
    onSurfaceVariant = TextSecondary,
    surfaceContainer = DarkSurfaceContainer,
    surfaceContainerLow = DarkSurfaceContainerLow,
    surfaceContainerLowest = DarkSurfaceContainerLowest,
    surfaceContainerHigh = DarkSurfaceContainerHigh,
    surfaceContainerHighest = DarkSurfaceContainerHighest,
    outline = OutlineBorder,
    outlineVariant = OutlineBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Developer tool defaults to high-contrast night mode
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = AlignerDarkColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = DarkBackground.toArgb()
            window.navigationBarColor = DarkBackground.toArgb()
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
