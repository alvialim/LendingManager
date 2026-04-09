package com.haftabook.app.presentation.auth

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

private const val PIN_LEN = 4

@Composable
fun SetNewPinScreen(
    onNewPinConfirmed: (pin: String) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var phase by remember { mutableIntStateOf(0) }
    var first by remember { mutableStateOf("") }
    var second by remember { mutableStateOf("") }
    var mismatchError by remember { mutableStateOf(false) }

    val dotsFilled = if (phase == 0) first.length else second.length

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = if (phase == 0) "Set new PIN" else "Confirm new PIN",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Enter the same $PIN_LEN-digit PIN twice.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (mismatchError) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "PINs did not match. Try again.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(Modifier.height(8.dp))
            TextButton(
                onClick = {
                    mismatchError = false
                    phase = 0
                    first = ""
                    second = ""
                },
            ) {
                Text("Start over")
            }
        }
        Spacer(Modifier.height(24.dp))
        PinDotsRow(length = PIN_LEN, filled = dotsFilled)
        Spacer(Modifier.height(32.dp))
        PinKeypad(
            modifier = Modifier.fillMaxWidth(),
            onDigit = { d ->
                if (phase == 0) {
                    if (first.length < PIN_LEN) {
                        first += d
                        if (first.length == PIN_LEN) phase = 1
                    }
                } else {
                    if (second.length < PIN_LEN) {
                        second += d
                        if (second.length == PIN_LEN) {
                            if (first == second) {
                                onNewPinConfirmed(second)
                            } else {
                                mismatchError = true
                                second = ""
                            }
                        }
                    }
                }
            },
            onBackspace = {
                if (phase == 0) {
                    if (first.isNotEmpty()) first = first.dropLast(1)
                } else {
                    if (second.isNotEmpty()) second = second.dropLast(1)
                    else phase = 0
                }
            },
        )
        Spacer(Modifier.height(24.dp))
        TextButton(onClick = onCancel) {
            Text("Cancel")
        }
    }
}
