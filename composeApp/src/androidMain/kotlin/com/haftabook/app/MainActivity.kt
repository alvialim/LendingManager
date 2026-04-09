package com.haftabook.app

import android.Manifest
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import com.haftabook.app.utils.ForgotPinSmsPermissionBridge
import com.haftabook.app.utils.ForgotPinSmsSender

/**
 * Registers [ActivityResultContracts.RequestPermission] on the Activity (not inside Compose).
 * Per AndroidX Activity, this must be registered while the Activity is being initialized
 * (field initializer runs before [onCreate]).
 */
class MainActivity : ComponentActivity() {

    private val smsPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        val otp = ForgotPinSmsPermissionBridge.pendingOtp
        ForgotPinSmsPermissionBridge.pendingOtp = null
        if (granted && otp != null) {
            ForgotPinSmsSender.sendNow(this, otp)
        } else if (!granted) {
            Toast.makeText(
                this,
                "Allow SMS so the app can send the OTP in the background.",
                Toast.LENGTH_LONG,
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        AndroidAppContext.activity = this

        ForgotPinSmsPermissionBridge.requestSmsPermissions = {
            smsPermissionLauncher.launch(Manifest.permission.SEND_SMS)
        }

        setContent {
            AppRoot()
        }
    }

    override fun onDestroy() {
        if (AndroidAppContext.activity === this) {
            AndroidAppContext.activity = null
        }
        super.onDestroy()
    }
}
