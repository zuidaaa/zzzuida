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

private val UnifiedDarkColorScheme = darkColorScheme(
    primary = LuminousBlue,
    onPrimary = AbsoluteBlack,
    primaryContainer = Color(0xFF1E2A4A),
    onPrimaryContainer = Color(0xFFD0DFFF),
    secondary = VibrantTeal,
    onSecondary = AbsoluteBlack,
    secondaryContainer = Color(0xFF00382E),
    onSecondaryContainer = Color(0xFF80FFEF),
    tertiary = VibrantPurple,
    onTertiary = AbsoluteBlack,
    tertiaryContainer = Color(0xFF4A1E66),
    onTertiaryContainer = Color(0xFFF8B0FF),
    background = AbsoluteBlack,
    onBackground = TextPrimary,
    surface = GlassCardPanel,
    onSurface = TextPrimary,
    surfaceVariant = Color(0xFF151515),
    onSurfaceVariant = TextSecondary,
    outline = BorderSubtle,
    outlineVariant = BorderSubtle.copy(alpha = 0.5f)
)

private val UnifiedLightColorScheme = lightColorScheme(
    primary = Color(0xFF1A3BB0),
    onPrimary = Color(0xFFFFFFFF),
    background = Color(0xFFFFFBFF),
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFBFF),
    onSurface = Color(0xFF1C1B1F)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, 
    dynamicColor: Boolean = false, // Disabled by default for design system consistency
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> UnifiedDarkColorScheme
        else -> UnifiedLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
