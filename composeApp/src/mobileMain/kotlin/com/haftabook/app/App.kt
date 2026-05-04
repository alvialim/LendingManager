package com.haftabook.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.haftabook.app.di.AppContainer
import com.haftabook.app.platform.SystemBarsTheme
import com.haftabook.app.preferences.ThemePreferences
import com.haftabook.app.presentation.navigation.AppNavigation
import com.haftabook.app.ui.HaftabookTheme

@Composable
fun App(container: AppContainer) {
    var isDarkTheme by remember { mutableStateOf(ThemePreferences.isDarkTheme()) }
    var isShowMonthlyEnabled by remember { mutableStateOf(ThemePreferences.isShowMonthlyEnabled()) }

    HaftabookTheme(darkTheme = isDarkTheme) {
        SystemBarsTheme(darkTheme = isDarkTheme)
        Surface(color = MaterialTheme.colorScheme.background) {
            AppNavigation(
                container = container,
                isDarkTheme = isDarkTheme,
                isShowMonthlyEnabled = isShowMonthlyEnabled,
                onDarkThemeChange = { dark ->
                    isDarkTheme = dark
                    ThemePreferences.setDarkTheme(dark)
                },
                onShowMonthlyChange = { showMonthly ->
                    isShowMonthlyEnabled = showMonthly
                    ThemePreferences.setShowMonthlyEnabled(showMonthly)
                }
            )
        }
    }
}
