package com.haftabook.app.preferences

expect object ThemePreferences {
    fun isDarkTheme(): Boolean
    fun setDarkTheme(value: Boolean)
}
