package com.haftabook.app.presentation.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.haftabook.app.utils.CustomerCommunicationText

private const val OTP_LEN = 6

@Composable
fun VerifyOtpScreen(
    onVerify: (otp: String) -> Unit,
    onResendOtp: () -> Unit,
    onBack: () -> Unit,
    errorMessage: String?,
    modifier: Modifier = Modifier,
) {
    var code by remember { mutableStateOf("") }
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Verify OTP",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Enter the $OTP_LEN-digit OTP. It matches the code in the SMS (sent to ${CustomerCommunicationText.PIN_RESET_OTP_NUMBER}).",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = code,
            onValueChange = { raw ->
                code = raw.filter { it.isDigit() }.take(OTP_LEN)
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("OTP") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
        if (errorMessage != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { onVerify(code) },
            enabled = code.length == OTP_LEN,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Verify")
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onResendOtp, modifier = Modifier.fillMaxWidth()) {
            Text("Resend OTP")
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Back")
        }
    }
}
