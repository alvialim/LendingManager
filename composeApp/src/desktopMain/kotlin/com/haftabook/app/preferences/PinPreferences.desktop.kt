package com.haftabook.app.preferences

import java.util.prefs.Preferences

private const val KEY_PIN_HASH = "pin_hash"
private const val KEY_PENDING_OTP = "pending_otp"
private const val KEY_PENDING_OTP_EXPIRES = "pending_otp_expires_at"

actual object PinPreferences {
    private val node: Preferences
        get() = Preferences.userRoot().node("com.haftabook.app")

    actual fun hasPin(): Boolean = !getPinHash().isNullOrBlank()

    actual fun getPinHash(): String? = node.get(KEY_PIN_HASH, null)?.takeIf { it.isNotBlank() }

    actual fun savePinHash(hash: String) {
        node.put(KEY_PIN_HASH, hash)
    }

    actual fun clearPin() {
        node.remove(KEY_PIN_HASH)
    }

    actual fun getPendingOtp(): String? = node.get(KEY_PENDING_OTP, null)

    actual fun getPendingOtpExpiresAtEpochMs(): Long =
        runCatching { node.getLong(KEY_PENDING_OTP_EXPIRES, 0L) }.getOrDefault(0L)

    actual fun setPendingOtp(otp: String, expiresAtEpochMs: Long) {
        node.put(KEY_PENDING_OTP, otp)
        node.putLong(KEY_PENDING_OTP_EXPIRES, expiresAtEpochMs)
    }

    actual fun clearPendingOtp() {
        node.remove(KEY_PENDING_OTP)
        node.remove(KEY_PENDING_OTP_EXPIRES)
    }
}
