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
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

fun main() = application {
    DesktopAppContext.database = getDesktopDatabaseBuilder().build()
    runCatching {
        ensureDesktopFirebaseInitialized()
        val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        DesktopAppContext.networkMonitor = NetworkMonitor().also { it.start() }
        DesktopAppContext.firebaseSyncEngine = FirebaseSyncEngine(
            db = DesktopAppContext.database,
            networkMonitor = DesktopAppContext.networkMonitor
        )
        DesktopAppContext.firebaseSyncEngine.start(applicationScope)
    }.onFailure { e ->
        println("[StarGroup][Desktop] Startup failed: ${e.message}")
        e.printStackTrace()
        runCatching {
            val dir = File(System.getProperty("user.home"), ".star-group")
            dir.mkdirs()
            val out = File(dir, "desktop-startup-error.txt")
            val sw = StringWriter()
            e.printStackTrace(PrintWriter(sw))
            out.writeText(sw.toString())
        }
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = "Star Group",
    ) {
        AppRoot()
    }
}
