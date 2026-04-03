package com.haftabook.app

import androidx.compose.runtime.Composable
import com.haftabook.app.di.AppContainer

@Composable
actual fun AppRoot() {
    App(
        AppContainer(
            database = DesktopAppContext.database,
            networkMonitor = DesktopAppContext.networkMonitor,
            onRequestSync = { DesktopAppContext.firebaseSyncEngine.flushPendingSyncToCloud() }
        )
    )
}
