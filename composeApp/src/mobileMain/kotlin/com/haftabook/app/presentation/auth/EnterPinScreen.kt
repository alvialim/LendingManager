package com.haftabook.app.presentation.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.haftabook.app.presentation.components.ResponsiveCentered
enum class PinEntryMode {
    /** First launch: create a new PIN. */
    Create,

    /** Normal unlock. */
    Enter,
}

private const val PIN_LEN = 4

@Composable
fun EnterPinScreen(
    mode: PinEntryMode,
    onPinComplete: (pin: String) -> Unit,
    errorMessage: String?,
    showForgotPin: Boolean = false,
    onForgotPin: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var digits by remember { mutableStateOf("") }
    LaunchedEffect(errorMessage) {
        if (errorMessage != null) digits = ""
    }
    val setDigits: (String) -> Unit = { new ->
        digits = new
        if (new.length == PIN_LEN) {
            onPinComplete(new)
        }
    }

    ResponsiveCentered(modifier = modifier) { inner ->
        Column(
            modifier = inner
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = when (mode) {
                    PinEntryMode.Create -> "Create PIN"
                    PinEntryMode.Enter -> "Enter PIN"
                },
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Use the keypad — $PIN_LEN digits",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(24.dp))
            PinDotsRow(length = PIN_LEN, filled = digits.length)
            if (errorMessage != null) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(Modifier.height(32.dp))
            PinKeypad(
                modifier = Modifier.fillMaxWidth(),
                onDigit = { d ->
                    if (digits.length < PIN_LEN) setDigits(digits + d)
                },
                onBackspace = {
                    if (digits.isNotEmpty()) setDigits(digits.dropLast(1))
                },
            )
            if (mode == PinEntryMode.Enter && showForgotPin) {
                Spacer(Modifier.height(24.dp))
                TextButton(onClick = onForgotPin) {
                    Text("Forgot PIN?")
                }
            }
        }
    }
}
