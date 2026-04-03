package com.haftabook.app

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.haftabook.app.data.getDesktopDatabaseBuilder
import com.haftabook.app.data.sync.FirebaseSyncEngine
import com.haftabook.app.network.NetworkMonitor
import com.haftabook.app.desktop.ensureDesktopFirebaseInitialized
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

fun main() = application {
    DesktopAppContext.database = getDesktopDatabaseBuilder().build()
    ensureDesktopFirebaseInitialized()
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    DesktopAppContext.networkMonitor = NetworkMonitor().also { it.start() }
    DesktopAppContext.firebaseSyncEngine = FirebaseSyncEngine(
        db = DesktopAppContext.database,
        networkMonitor = DesktopAppContext.networkMonitor
    )
    DesktopAppContext.firebaseSyncEngine.start(applicationScope)

    Window(
        onCloseRequest = ::exitApplication,
        title = "Haftabook",
    ) {
        AppRoot()
    }
}
