package com.haftabook.app

import androidx.compose.runtime.Composable
import com.haftabook.app.di.AppContainer

/**
 * SMS permission is registered on [MainActivity] (not here) so the Activity Result API
 * is wired to the Activity lifecycle correctly.
 */
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
