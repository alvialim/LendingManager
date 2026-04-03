package com.haftabook.app

import com.haftabook.app.data.AppDatabase
import com.haftabook.app.data.sync.FirebaseSyncEngine
import com.haftabook.app.network.NetworkMonitor

object DesktopAppContext {
    lateinit var database: AppDatabase
    lateinit var networkMonitor: NetworkMonitor
    lateinit var firebaseSyncEngine: FirebaseSyncEngine
}
