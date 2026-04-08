package com.haftabook.app.platform

import android.Manifest
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun RequestMediaPermissionsOnHome() {
    val context = LocalContext.current
    val activity = context as? ComponentActivity ?: return

    val perms = buildList {
        add(Manifest.permission.CAMERA)
        if (Build.VERSION.SDK_INT >= 33) {
            add(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            add(Manifest.permission.READ_EXTERNAL_STORAGE)
            if (Build.VERSION.SDK_INT <= 28) {
                @Suppress("DEPRECATION")
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    }.toTypedArray()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { /* no-op */ }
    )

    LaunchedEffect(Unit) {
        launcher.launch(perms)
    }
}

