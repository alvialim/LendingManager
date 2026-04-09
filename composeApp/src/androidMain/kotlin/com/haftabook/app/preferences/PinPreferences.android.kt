package com.haftabook.app.preferences

import android.content.Context
import com.haftabook.app.AndroidAppContext

private const val PREF_NAME = "haftabook_prefs"
private const val KEY_PIN_HASH = "pin_hash"
private const val KEY_PENDING_OTP = "pending_otp"
private const val KEY_PENDING_OTP_EXPIRES = "pending_otp_expires_at"

private fun prefs() =
    AndroidAppContext.applicationContext?.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

actual object PinPreferences {
    actual fun hasPin(): Boolean = !getPinHash().isNullOrBlank()

    actual fun getPinHash(): String? = prefs()?.getString(KEY_PIN_HASH, null)

    actual fun savePinHash(hash: String) {
        prefs()?.edit()?.putString(KEY_PIN_HASH, hash)?.apply()
    }

    actual fun clearPin() {
        prefs()?.edit()?.remove(KEY_PIN_HASH)?.apply()
    }

    actual fun getPendingOtp(): String? = prefs()?.getString(KEY_PENDING_OTP, null)

    actual fun getPendingOtpExpiresAtEpochMs(): Long =
        prefs()?.getLong(KEY_PENDING_OTP_EXPIRES, 0L) ?: 0L

    actual fun setPendingOtp(otp: String, expiresAtEpochMs: Long) {
        prefs()?.edit()
            ?.putString(KEY_PENDING_OTP, otp)
            ?.putLong(KEY_PENDING_OTP_EXPIRES, expiresAtEpochMs)
            ?.apply()
    }

    actual fun clearPendingOtp() {
        prefs()?.edit()?.remove(KEY_PENDING_OTP)?.remove(KEY_PENDING_OTP_EXPIRES)?.apply()
    }
}
