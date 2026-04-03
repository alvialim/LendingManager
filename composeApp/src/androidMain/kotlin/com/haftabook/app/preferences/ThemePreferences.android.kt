package com.haftabook.app.preferences

import android.content.Context
import com.haftabook.app.AndroidAppContext

private const val PREF_NAME = "haftabook_prefs"
private const val KEY_DARK_THEME = "dark_theme"

actual object ThemePreferences {
    actual fun isDarkTheme(): Boolean =
        AndroidAppContext.applicationContext
            ?.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            ?.getBoolean(KEY_DARK_THEME, false)
            ?: false

    actual fun setDarkTheme(value: Boolean) {
        AndroidAppContext.applicationContext
            ?.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            ?.edit()
            ?.putBoolean(KEY_DARK_THEME, value)
            ?.apply()
    }
}
