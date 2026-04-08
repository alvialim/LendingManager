package com.haftabook.app.presentation.components

import androidx.compose.ui.graphics.Color

internal fun placeholderInitial(displayName: String?): String {
    val c = displayName?.trim()?.firstOrNull { !it.isWhitespace() } ?: return "?"
    return c.uppercase()
}

internal fun placeholderColor(displayName: String?): Color {
    val palette = listOf(
        Color(0xFF60A5FA), // blue
        Color(0xFF34D399), // green
        Color(0xFFFBBF24), // amber
        Color(0xFFF87171), // red
        Color(0xFFA78BFA), // purple
        Color(0xFF22D3EE), // cyan
        Color(0xFFFB7185), // rose
        Color(0xFF4ADE80), // emerald
    )
    val key = displayName?.trim().orEmpty()
    val idx = (key.hashCode().absoluteValue) % palette.size
    return palette[idx]
}

private val Int.absoluteValue: Int get() = if (this < 0) -this else this

