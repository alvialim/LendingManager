package com.haftabook.app.web

import com.haftabook.app.data.sync.HAFTABOOK_REALTIME_DATABASE_URL
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions

/**
 * GitLive Firebase on JS requires explicit [Firebase.initialize]; Android uses `google-services.json`.
 *
 * Values mirror [composeApp/google-services.json]. If initialization fails, add a **Web** app in
 * Firebase Console (Project settings → Your apps) and set [WEB_FIREBASE_APP_ID] to its `appId` (contains `:web:`).
 *
 * Uses top-level state instead of `object` to avoid Kotlin 2.1 K2/JS [FirObjectConstructorChecker] crashes.
 */
private var webFirebaseInitialized: Boolean = false

private const val WEB_FIREBASE_API_KEY: String = "AIzaSyDRFPTlprrLo9k_DU-R00Z8XtXGEEvdJPU"
private const val WEB_FIREBASE_PROJECT_ID: String = "haftabookkmp"
private const val WEB_FIREBASE_STORAGE_BUCKET: String = "haftabookkmp.firebasestorage.app"
private const val WEB_FIREBASE_GCM_SENDER_ID: String = "448883099305"
private const val WEB_FIREBASE_AUTH_DOMAIN: String = "haftabookkmp.firebaseapp.com"
private const val WEB_FIREBASE_APP_ID: String = "1:448883099305:android:582f72e32b87082eb1cbb8"

internal fun ensureWebFirebaseInitialized() {
    if (webFirebaseInitialized) return
    Firebase.initialize(
        context = null,
        options = FirebaseOptions(
            applicationId = WEB_FIREBASE_APP_ID,
            apiKey = WEB_FIREBASE_API_KEY,
            databaseUrl = HAFTABOOK_REALTIME_DATABASE_URL,
            projectId = WEB_FIREBASE_PROJECT_ID,
            storageBucket = WEB_FIREBASE_STORAGE_BUCKET,
            gcmSenderId = WEB_FIREBASE_GCM_SENDER_ID,
            authDomain = WEB_FIREBASE_AUTH_DOMAIN,
        )
    )
    webFirebaseInitialized = true
}
