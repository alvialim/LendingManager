package com.haftabook.app.presentation.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.haftabook.app.platform.PlatformBackHandler
import com.haftabook.app.preferences.PinPreferences
import com.haftabook.app.security.PinCrypto
import com.haftabook.app.utils.CommunicationHelper
import kotlin.random.Random

private sealed interface AuthRoute {
    data object CreatePin : AuthRoute
    data object EnterPin : AuthRoute
    data object ForgotPin : AuthRoute
    data object VerifyOtp : AuthRoute
    data object SetNewPin : AuthRoute
}

private const val OTP_VALID_MS = 10 * 60 * 1_000L

private fun issueOtpAndSend() {
    val otp = Random.nextInt(100_000, 1_000_000).toString()
    val expiresAt = System.currentTimeMillis() + OTP_VALID_MS
    PinPreferences.setPendingOtp(otp, expiresAt)
    CommunicationHelper.sendForgotPinOtp(otp)
}

private fun verifyOtpInput(input: String): Boolean {
    val pending = PinPreferences.getPendingOtp() ?: return false
    val exp = PinPreferences.getPendingOtpExpiresAtEpochMs()
    if (System.currentTimeMillis() > exp) return false
    return input == pending
}

@Composable
fun AuthFlow(
    onUnlocked: () -> Unit,
) {
    val startHasPin = remember { PinPreferences.hasPin() }
    var route by remember {
        mutableStateOf<AuthRoute>(
            if (startHasPin) AuthRoute.EnterPin else AuthRoute.CreatePin
        )
    }
    var enterPinError by remember { mutableStateOf<String?>(null) }
    var verifyOtpError by remember { mutableStateOf<String?>(null) }
    var sendingOtp by remember { mutableStateOf(false) }

    PlatformBackHandler(enabled = route == AuthRoute.ForgotPin) {
        route = AuthRoute.EnterPin
    }
    PlatformBackHandler(enabled = route == AuthRoute.VerifyOtp) {
        route = AuthRoute.ForgotPin
        verifyOtpError = null
    }
    PlatformBackHandler(enabled = route == AuthRoute.SetNewPin) {
        PinPreferences.clearPendingOtp()
        route = AuthRoute.EnterPin
    }

    when (route) {
        AuthRoute.CreatePin -> {
            EnterPinScreen(
                mode = PinEntryMode.Create,
                onPinComplete = { pin ->
                    PinPreferences.savePinHash(PinCrypto.hashPin(pin))
                    onUnlocked()
                },
                onForgotPin = { },
                errorMessage = null,
            )
        }

        AuthRoute.EnterPin -> {
            EnterPinScreen(
                mode = PinEntryMode.Enter,
                onPinComplete = { pin ->
                    val h = PinPreferences.getPinHash()
                    if (h != null && PinCrypto.hashPin(pin) == h) {
                        enterPinError = null
                        onUnlocked()
                    } else {
                        enterPinError = "Wrong PIN"
                    }
                },
                onForgotPin = {
                    enterPinError = null
                    route = AuthRoute.ForgotPin
                },
                errorMessage = enterPinError,
            )
        }

        AuthRoute.ForgotPin -> {
            ResetPinScreen(
                onSendOtp = {
                    sendingOtp = true
                    verifyOtpError = null
                    issueOtpAndSend()
                    sendingOtp = false
                    route = AuthRoute.VerifyOtp
                },
                onBack = {
                    route = AuthRoute.EnterPin
                },
                isSending = sendingOtp,
            )
        }

        AuthRoute.VerifyOtp -> {
            VerifyOtpScreen(
                onVerify = { code ->
                    if (verifyOtpInput(code)) {
                        verifyOtpError = null
                        route = AuthRoute.SetNewPin
                    } else {
                        verifyOtpError = "Invalid or expired OTP"
                    }
                },
                onResendOtp = {
                    verifyOtpError = null
                    issueOtpAndSend()
                },
                onBack = {
                    route = AuthRoute.ForgotPin
                    verifyOtpError = null
                },
                errorMessage = verifyOtpError,
            )
        }

        AuthRoute.SetNewPin -> {
            SetNewPinScreen(
                onNewPinConfirmed = { pin ->
                    PinPreferences.savePinHash(PinCrypto.hashPin(pin))
                    PinPreferences.clearPendingOtp()
                    onUnlocked()
                },
                onCancel = {
                    PinPreferences.clearPendingOtp()
                    route = AuthRoute.EnterPin
                },
            )
        }
    }
}
