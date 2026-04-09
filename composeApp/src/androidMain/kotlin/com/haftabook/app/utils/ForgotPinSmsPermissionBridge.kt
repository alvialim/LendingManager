package com.haftabook.app.utils

/**
 * [com.haftabook.app.MainActivity] assigns [requestSmsPermissions] to launch
 * [android.Manifest.permission.SEND_SMS] after [sendForgotPinOtpInBackgroundOrRequest] stores [pendingOtp].
 */
object ForgotPinSmsPermissionBridge {
    var pendingOtp: String? = null
    var requestSmsPermissions: (() -> Unit)? = null
}
