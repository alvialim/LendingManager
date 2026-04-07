package com.haftabook.app.desktop

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.FirebasePlatform
import com.haftabook.app.data.sync.HAFTABOOK_REALTIME_DATABASE_URL

/**
 * GitLive on JVM uses firebase-java-sdk: [FirebasePlatform] then [FirebaseApp.initializeApp]
 * (see GitLive / firebase-java-sdk README). Common [dev.gitlive.firebase.Firebase.initialize] is not on the JVM API.
 * Values align with [composeApp/google-services.json] and [com.haftabook.app.web.ensureWebFirebaseInitialized].
 */
private var desktopFirebaseInitialized: Boolean = false

private const val FIREBASE_API_KEY: String = "AIzaSyDRFPTlprrLo9k_DU-R00Z8XtXGEEvdJPU"
private const val FIREBASE_PROJECT_ID: String = "haftabookkmp"
private const val FIREBASE_STORAGE_BUCKET: String = "haftabookkmp.firebasestorage.app"
private const val FIREBASE_GCM_SENDER_ID: String = "448883099305"
private const val FIREBASE_APP_ID: String = "1:448883099305:android:582f72e32b87082eb1cbb8"

internal fun ensureDesktopFirebaseInitialized() {
    if (desktopFirebaseInitialized) return
    runCatching {
        FirebasePlatform.initializeFirebasePlatform(object : FirebasePlatform() {
            private val storage = mutableMapOf<String, String>()
            override fun store(key: String, value: String) {
                storage[key] = value
            }

            override fun retrieve(key: String): String? = storage[key]

            override fun clear(key: String) {
                storage.remove(key)
            }

            override fun log(msg: String) {
                println("[Haftabook][Firebase] $msg")
            }
        })

        val options = FirebaseOptions.Builder()
            .setApplicationId(FIREBASE_APP_ID)
            .setApiKey(FIREBASE_API_KEY)
            .setDatabaseUrl(HAFTABOOK_REALTIME_DATABASE_URL)
            .setProjectId(FIREBASE_PROJECT_ID)
            .setStorageBucket(FIREBASE_STORAGE_BUCKET)
            .setGcmSenderId(FIREBASE_GCM_SENDER_ID)
            .build()

        // GitLive JVM (firebase-java-sdk) exposes Android-like stubs; init requires a Context.
        val app = Application()
        if (FirebaseApp.getApps(app).isEmpty()) {
            FirebaseApp.initializeApp(app, options)
        }

        desktopFirebaseInitialized = true
    }.onFailure { e ->
        // Keep running, but make failure visible in packaged desktop apps.
        println("[Haftabook][Firebase] Desktop init failed: ${e.message}")
        e.printStackTrace()
    }
}
