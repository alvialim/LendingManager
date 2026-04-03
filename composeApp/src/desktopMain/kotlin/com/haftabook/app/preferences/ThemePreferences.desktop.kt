package com.haftabook.app.preferences

import java.util.prefs.Preferences

private const val KEY_DARK_THEME = "dark_theme"

actual object ThemePreferences {
    private val node: Preferences
        get() = Preferences.userRoot().node("com.haftabook.app")

    actual fun isDarkTheme(): Boolean = node.getBoolean(KEY_DARK_THEME, false)

    actual fun setDarkTheme(value: Boolean) {
        node.putBoolean(KEY_DARK_THEME, value)
    }
}
