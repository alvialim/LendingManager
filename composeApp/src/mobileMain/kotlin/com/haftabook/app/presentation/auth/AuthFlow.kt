package com.haftabook.app.presentation.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.haftabook.app.data.remote.PinManagementConfig
import com.haftabook.app.data.remote.PinType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun AuthFlow(
    pinType: PinType,
    onUnlocked: () -> Unit,
) {
    var expectedPin by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var enterPinError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(pinType) {
        loading = true
        loadError = null
        expectedPin = null
        val result = runCatching {
            withContext(Dispatchers.IO) {
                PinManagementConfig.fetchPin(pinType)
            }
        }
        loading = false
        result.onSuccess { pin ->
            expectedPin = pin
            if (pin.isBlank()) {
                loadError = "PIN not configured in Firebase"
            }
        }.onFailure {
            loadError = it.message ?: "Unable to load PIN from Firebase"
        }
    }

    when {
        loading -> {
            EnterPinScreen(
                mode = PinEntryMode.Enter,
                onPinComplete = { },
                errorMessage = null,
            )
        }

        loadError != null -> {
            EnterPinScreen(
                mode = PinEntryMode.Enter,
                onPinComplete = { },
                errorMessage = loadError,
            )
        }

        else -> {
            EnterPinScreen(
                mode = PinEntryMode.Enter,
                onPinComplete = { pin ->
                    if (pin == expectedPin) {
                        enterPinError = null
                        onUnlocked()
                    } else {
                        enterPinError = "Wrong PIN"
                    }
                },
                errorMessage = enterPinError,
            )
        }
    }
}
