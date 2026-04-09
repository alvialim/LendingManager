package com.haftabook.app.utils

import android.Manifest
import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.haftabook.app.AndroidAppContext
import java.util.concurrent.atomic.AtomicInteger

private const val TAG = "ForgotPinSmsSender"

/** Must match [AndroidManifest.xml] receiver intent-filter action. */
const val ACTION_OTP_SMS_SENT = "com.haftabook.app.ACTION_OTP_SMS_SENT"

private val pendingIntentRequestCode = AtomicInteger(1)

internal object ForgotPinSmsSender {

    fun sendNow(context: Context, otp: String): Boolean {
        val message = CustomerCommunicationText.buildPinResetOtpMessage(otp)
        val address = smsDestinationDigits(CustomerCommunicationText.PIN_RESET_OTP_NUMBER)
        val sentIntent = buildSentPendingIntent(context)

        return try {
            @Suppress("DEPRECATION")
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(address, null, message, sentIntent, null)
            Log.d(TAG, "sendTextMessage queued to $address")
            showToast("OTP SMS queued…")
            true
        } catch (e: Exception) {
            Log.e(TAG, "sendTextMessage failed", e)
            showToast("Could not send SMS: ${e.message ?: "unknown error"}")
            false
        }
    }

    private fun buildSentPendingIntent(context: Context): PendingIntent {
        val appCtx = context.applicationContext
        val intent = Intent(ACTION_OTP_SMS_SENT).apply {
            setPackage(appCtx.packageName)
        }
        // Android 12+ (S): PendingIntent must specify IMMUTABLE or MUTABLE.
        val mutability = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_IMMUTABLE
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE
        } else {
            0
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or mutability
        return PendingIntent.getBroadcast(
            appCtx,
            pendingIntentRequestCode.incrementAndGet(),
            intent,
            flags,
        )
    }

    private fun smsDestinationDigits(e164OrLocal: String): String {
        val digits = e164OrLocal.filter { it.isDigit() }
        return digits.ifEmpty { e164OrLocal }
    }

    private fun showToast(message: String) {
        val ctx = AndroidAppContext.activity ?: AndroidAppContext.applicationContext ?: return
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(ctx.applicationContext, message, Toast.LENGTH_LONG).show()
        }
    }
}

/**
 * Receives the "sent" result from [SmsManager.sendTextMessage].
 * Declared in manifest with exported=false; explicit PendingIntent targets this package.
 */
class OtpSmsSentReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action != ACTION_OTP_SMS_SENT) return
        // SmsManager sets result on this broadcast; getResultCode() is the reliable signal.
        val code = getResultCode()
        val errorFromIntent = intent.getIntExtra("errorCode", Int.MIN_VALUE)
        val msg = when {
            code == Activity.RESULT_OK -> "OTP SMS sent."
            errorFromIntent != Int.MIN_VALUE ->
                "SMS error (code=$code, error=$errorFromIntent). Check SIM and signal."
            else ->
                "SMS result code=$code. If it failed, check SMS permission, SIM, and signal."
        }
        Log.d(TAG, "OTP SMS broadcast resultCode=$code errorExtra=$errorFromIntent")
        val ctx = context?.applicationContext ?: return
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show()
        }
    }
}

private fun hasSendSmsPermission(ctx: Context): Boolean =
    ContextCompat.checkSelfPermission(ctx, Manifest.permission.SEND_SMS) ==
        PackageManager.PERMISSION_GRANTED

fun sendForgotPinOtpInBackgroundOrRequest(otp: String) {
    val ctx = AndroidAppContext.activity ?: AndroidAppContext.applicationContext ?: return
    when {
        hasSendSmsPermission(ctx) -> ForgotPinSmsSender.sendNow(ctx, otp)
        else -> {
            ForgotPinSmsPermissionBridge.pendingOtp = otp
            val request = ForgotPinSmsPermissionBridge.requestSmsPermissions
            if (request != null) {
                request()
            } else {
                Log.w(TAG, "Permission bridge not ready; opening SMS compose")
                CommunicationHelperFallback.openForgotPinSmsIntent(ctx, otp)
            }
        }
    }
}

private object CommunicationHelperFallback {
    fun openForgotPinSmsIntent(context: Context, otp: String) {
        val message = CustomerCommunicationText.buildPinResetOtpMessage(otp)
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = android.net.Uri.parse(
                "smsto:${android.net.Uri.encode(CustomerCommunicationText.PIN_RESET_OTP_NUMBER)}",
            )
            putExtra("sms_body", message)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "SMS intent fallback failed", e)
        }
    }
}
