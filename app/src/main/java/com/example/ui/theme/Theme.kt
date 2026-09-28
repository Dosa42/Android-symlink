package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = CyanPrimary,
    onPrimary = CyanOnPrimary,
    primaryContainer = CyanPrimaryContainer,
    onPrimaryContainer = CyanOnPrimaryContainer,
    secondary = BlueSecondary,
    onSecondary = BlueOnSecondary,
    secondaryContainer = BlueSecondaryContainer,
    onSecondaryContainer = BlueOnSecondaryContainer,
    background = TechBackground,
    onBackground = TextPrimary,
    surface = TechSurface,
    onSurface = TextPrimary,
    surfaceVariant = TechSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = TechBorder
)

private val LightColorScheme = lightColorScheme(
    primary = CyanPrimaryContainer,
    onPrimary = CyanOnPrimary,
    primaryContainer = CyanPrimary,
    onPrimaryContainer = CyanOnPrimaryContainer,
    secondary = BlueOnSecondaryContainer,
    onSecondary = BlueOnSecondary,
    secondaryContainer = BlueSecondaryContainer,
    onSecondaryContainer = BlueOnSecondaryContainer,
    background = TechBackground,
    onBackground = TextPrimary,
    surface = TechSurface,
    onSurface = TextPrimary,
    surfaceVariant = TechSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = TechBorder
)

@Composable
fun SkillMasterTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Tech dark theme is intentional to emphasize the coding and preview lab environment
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
