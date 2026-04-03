package com.haftabook.app

import androidx.compose.runtime.Composable
import com.haftabook.app.di.AppContainer

@Composable
actual fun AppRoot() {
    val ctx = AndroidAppContext.applicationContext ?: error("Application context not set")
    val app = ctx.applicationContext as HaftaBookApp
    App(
        AppContainer(
            database = app.database,
            networkMonitor = app.networkMonitor,
            onRequestSync = { app.firebaseSyncEngine.flushPendingSyncToCloud() }
        )
    )
}
