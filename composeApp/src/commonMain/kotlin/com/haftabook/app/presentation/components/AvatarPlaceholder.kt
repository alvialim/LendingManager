package com.haftabook.app.presentation.components

import androidx.compose.ui.graphics.Color

internal fun placeholderInitial(displayName: String?): String {
    val c = displayName?.trim()?.firstOrNull { !it.isWhitespace() } ?: return "?"
    return c.uppercase()
}

internal fun placeholderColor(displayName: String?): Color {
    val palette = listOf(
        Color(0xFF002174), // blue
        Color(0xFF007F51), // green
        Color(0xFF6F5200), // amber
        Color(0xFF7F0000), // red
        Color(0xFF350071), // purple
        Color(0xFF005F6C), // cyan
        Color(0xFF6F0014), // rose
        Color(0xFF006A25), // emerald
    )
    val key = displayName?.trim().orEmpty()
    val idx = (key.hashCode().absoluteValue) % palette.size
    return palette[idx]
}

private val Int.absoluteValue: Int get() = if (this < 0) -this else this

