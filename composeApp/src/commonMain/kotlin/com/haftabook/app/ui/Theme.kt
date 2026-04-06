package com.haftabook.app.ui

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Paid totals and EMI/payment amounts (labels “Paid”, EMI ₹, etc.) app-wide. */
val PaidAmountGreen = Color(0xFF388E3C)

/** Floating action buttons: blue fill, white + icon. */
val FabBlue = Color(0xFF0D6EFD)

// Bright off‑white + bright blue palette used across Android, Desktop and Web.
private val BrandBlue = Color(0xFF0D6EFD)      // primary bright blue
private val BrandBlueDark = Color(0xFF0B5ED7) // darker for pressed / outlines
private val BrandBlueLight = Color(0xFFE7F1FF)
private val OffWhite = Color(0xFFF9FAFB)      // screen background

private val HaftabookLightColors: ColorScheme = lightColorScheme(
    primary = BrandBlue,
    onPrimary = Color.White,
    primaryContainer = BrandBlueLight,
    onPrimaryContainer = BrandBlueDark,

    secondary = BrandBlueDark,
    onSecondary = Color.White,
    secondaryContainer = BrandBlueLight,
    onSecondaryContainer = BrandBlueDark,

    background = OffWhite,
    onBackground = Color(0xFF111827),

    surface = OffWhite,
    onSurface = Color(0xFF111827),
    surfaceVariant = Color(0xFFF2F4F7),
    onSurfaceVariant = Color(0xFF4B5563),

    outline = BrandBlueLight,
    outlineVariant = Color(0xFFD0D5DD),

    error = Color(0xFFDC2626),
    onError = Color.White,
)

private val HaftabookDarkColors: ColorScheme = darkColorScheme(
    primary = Color(0xFF6BA3FF),
    onPrimary = Color(0xFF0A1628),
    primaryContainer = Color(0xFF1E3A5F),
    onPrimaryContainer = Color(0xFFDCE9FF),

    secondary = Color(0xFF8AB4FF),
    onSecondary = Color(0xFF0A1628),
    secondaryContainer = Color(0xFF2A4A6F),
    onSecondaryContainer = Color(0xFFE7F1FF),

    background = Color(0xFF121212),
    onBackground = Color(0xFFE8EAED),

    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE8EAED),
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = Color(0xFFC4C7CC),

    outline = Color(0xFF3D4450),
    outlineVariant = Color(0xFF2C3138),

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
)

@Composable
fun HaftabookTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) HaftabookDarkColors else HaftabookLightColors,
        typography = MaterialTheme.typography,
        shapes = MaterialTheme.shapes,
        content = content,
    )
}

