package com.haftabook.app.preferences

expect object PinPreferences {
    fun hasPin(): Boolean
    fun getPinHash(): String?
    fun savePinHash(hash: String)
    fun clearPin()

    fun getPendingOtp(): String?
    fun getPendingOtpExpiresAtEpochMs(): Long
    fun setPendingOtp(otp: String, expiresAtEpochMs: Long)
    fun clearPendingOtp()
}
