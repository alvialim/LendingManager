package com.haftabook.app

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.haftabook.app.data.AppDatabase
import com.haftabook.app.data.getDatabaseBuilder
import com.haftabook.app.data.sync.FirebaseSyncEngine
import com.haftabook.app.network.NetworkMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class HaftaBookApp : Application() {

    lateinit var database: AppDatabase
    lateinit var networkMonitor: NetworkMonitor
    lateinit var firebaseSyncEngine: FirebaseSyncEngine
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        // Required so GitLive’s Firebase Realtime Database uses a real default app + google-services config.
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this)
        }
        Log.d(TAG, "FirebaseApp default: ${FirebaseApp.getInstance().options.projectId}")
        AndroidAppContext.applicationContext = applicationContext
        database = getDatabaseBuilder(this).build()
        networkMonitor = NetworkMonitor().also { it.start() }
        firebaseSyncEngine = FirebaseSyncEngine(db = database, networkMonitor = networkMonitor)
        firebaseSyncEngine.start(applicationScope)
    }

    private companion object {
        private const val TAG = "HaftaBookApp"
    }
}
