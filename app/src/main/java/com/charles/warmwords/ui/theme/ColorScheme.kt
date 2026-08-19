package com.charles.warmwords.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = WarmOrange,
    onPrimary = TextOnPrimary,
    primaryContainer = SoftPeach,
    onPrimaryContainer = TextPrimary,
    secondary = WarmTeal,
    onSecondary = TextOnPrimary,
    secondaryContainer = WarmTealLight,
    onSecondaryContainer = TextPrimary,
    tertiary = WarmPink,
    onTertiary = TextPrimary,
    background = WarmCream,
    onBackground = TextPrimary,
    surface = CardBackground,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = TextSecondary,
    error = ErrorRed,
    onError = TextOnPrimary,
    errorContainer = ErrorRed.copy(alpha = 0.15f),
    onErrorContainer = TextPrimary
)

private val DarkColors = darkColorScheme(
    primary = WarmOrangeLight,
    onPrimary = Color(0xFF2A1500),
    primaryContainer = WarmOrangeDark,
    onPrimaryContainer = TextOnPrimary,
    secondary = WarmTealLight,
    onSecondary = Color(0xFF00201C),
    secondaryContainer = WarmTealDark,
    onSecondaryContainer = TextOnPrimary,
    tertiary = WarmLavender,
    onTertiary = Color(0xFF2A1533),
    background = WarmDarkBackground,
    onBackground = TextPrimaryDark,
    surface = CardBackgroundDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondaryDark,
    error = ErrorRed,
    onError = Color(0xFF3A0A08),
    errorContainer = ErrorRed.copy(alpha = 0.25f),
    onErrorContainer = TextPrimaryDark
)

val LightColorScheme: ColorScheme = LightColors
val DarkColorScheme: ColorScheme = DarkColors
