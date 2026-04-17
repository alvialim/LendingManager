package com.haftabook.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.haftabook.app.data.getDesktopDatabaseBuilder
import com.haftabook.app.data.sync.FirebaseSyncEngine
import com.haftabook.app.network.NetworkMonitor
import com.haftabook.app.desktop.ensureDesktopFirebaseInitialized
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Star Group",
    ) {
        var startupError by remember { mutableStateOf<String?>(null) }
        var isReady by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            runCatching {
                val db = withContext(Dispatchers.IO) {
                    getDesktopDatabaseBuilder().build()
                }
                DesktopAppContext.database = db

                // Firebase init/network/sync can be slow; do it off the UI thread.
                withContext(Dispatchers.Default) {
                    ensureDesktopFirebaseInitialized()
                }
                DesktopAppContext.networkMonitor = NetworkMonitor().also { it.start() }
                DesktopAppContext.firebaseSyncEngine = FirebaseSyncEngine(
                    db = DesktopAppContext.database,
                    networkMonitor = DesktopAppContext.networkMonitor
                )

                isReady = true
            }.onFailure { e ->
                val msg = e.message ?: e::class.simpleName ?: "Unknown error"
                startupError = msg
                println("[StarGroup][Desktop] Startup failed: $msg")
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
        }

        MaterialTheme {
            Surface(modifier = Modifier.fillMaxSize()) {
                when {
                    isReady -> AppRoot()
                    startupError != null -> {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text("Startup failed", style = MaterialTheme.typography.titleMedium)
                            Text(startupError ?: "", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "See ~/.star-group/desktop-startup-error.txt for details.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    else -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            CircularProgressIndicator()
                            Text("Starting…", modifier = Modifier.padding(top = 12.dp))
                        }
                    }
                }
            }
        }
    }
}
