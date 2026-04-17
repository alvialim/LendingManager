package com.haftabook.app

import com.haftabook.app.data.AppDatabase
import com.haftabook.app.data.sync.FirebaseSyncEngine
import com.haftabook.app.network.NetworkMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.util.concurrent.atomic.AtomicBoolean

object DesktopAppContext {
    lateinit var database: AppDatabase
    lateinit var networkMonitor: NetworkMonitor
    lateinit var firebaseSyncEngine: FirebaseSyncEngine

    private val syncStarted = AtomicBoolean(false)
    private val syncDispatcher by lazy { Dispatchers.Default.limitedParallelism(2) }
    private val syncScope by lazy { CoroutineScope(SupervisorJob() + syncDispatcher) }

    fun startBackgroundSyncIfNeeded() {
        if (!::firebaseSyncEngine.isInitialized) return
        if (!syncStarted.compareAndSet(false, true)) return
        firebaseSyncEngine.start(syncScope)
    }
}
